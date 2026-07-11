package com.k8s.cnapp.msa.analysis.service;

import com.k8s.cnapp.msa.analysis.infrastructure.alert.AlertWriter;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.DeploymentProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.EventProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.NamespaceProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.NodeProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.PodProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.ServiceProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.cache.AlertCacheInvalidationPort;
import com.k8s.cnapp.msa.analysis.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.analysis.infrastructure.tenant.TenantStore;
import com.k8s.cnapp.msa.analysis.model.Alert;
import com.k8s.cnapp.msa.analysis.model.AssetContext;
import com.k8s.cnapp.msa.analysis.model.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.model.EventProfile;
import com.k8s.cnapp.msa.analysis.model.NamespaceProfile;
import com.k8s.cnapp.msa.analysis.model.NodeProfile;
import com.k8s.cnapp.msa.analysis.model.ParsedSnapshot;
import com.k8s.cnapp.msa.analysis.model.PodProfile;
import com.k8s.cnapp.msa.analysis.model.Policy;
import com.k8s.cnapp.msa.analysis.model.ServiceProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import com.k8s.cnapp.msa.analysis.model.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.Category;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 기존 AnalysisService.processSnapshot 의 오케스트레이션을 동일한 순서/트랜잭션 의미로 수행한다.
 * K8s SDK 파싱은 consumer-kafka 어댑터(SnapshotParser)로 분리되었다.
 */
@Slf4j
@RequiredArgsConstructor
class ProcessSnapshotService implements ProcessSnapshotUseCase {

    private final TenantStore tenantStore;
    private final PolicyReader policyReader;
    private final PodProfileStore podProfileStore;
    private final NodeProfileStore nodeProfileStore;
    private final ServiceProfileStore serviceProfileStore;
    private final DeploymentProfileStore deploymentProfileStore;
    private final NamespaceProfileStore namespaceProfileStore;
    private final EventProfileStore eventProfileStore;
    private final AlertWriter alertWriter;
    private final PolicyEngine policyEngine;
    private final AlertCacheInvalidationPort alertCacheInvalidationPort;

    @Override
    @Transactional
    public void process(Long tenantId, ParsedSnapshot snapshot) {
        try {
            Tenant tenant = tenantStore.findById(tenantId)
                    .orElseGet(() -> tenantStore.save(new Tenant(tenantId, "Tenant-" + tenantId)));

            SecurityPolicyContext context = createDynamicContext(tenant);

            processPods(tenant, snapshot.pods(), context);
            processNodes(tenant, snapshot.nodes());
            processServices(tenant, snapshot.services());
            processDeployments(tenant, snapshot.deployments());
            processNamespaces(tenant, snapshot.namespaces());
            processEvents(tenant, snapshot.events());

            alertCacheInvalidationPort.invalidate(tenantId);
        } catch (Exception e) {
            log.error("[ANALYSIS-FATAL] Critical error: {}", e.getMessage(), e);
        }
    }

    private SecurityPolicyContext createDynamicContext(Tenant tenant) {
        List<Policy> dbPolicies = policyReader.findAllByTenant(tenant);
        Map<RuleType, String> ruleMap = dbPolicies.stream()
                .filter(Policy::isEnabled)
                .collect(Collectors.toMap(Policy::getRuleType, Policy::getValue, (v1, v2) -> v1));
        return new SecurityPolicyContext(tenant.getId(), ruleMap);
    }

    private void processPods(Tenant tenant, List<PodProfile> parsedPods, SecurityPolicyContext context) {
        if (parsedPods == null) return;
        List<PodProfile> profiles = new ArrayList<>();
        for (PodProfile parsed : parsedPods) {
            try {
                parsed.assignTenant(tenant);
                AssetContext assetContext = parsed.getAssetContext();

                PodProfile profile = podProfileStore.findByTenantAndAsset(tenant, assetContext.getNamespace(), assetContext.getPodName(), assetContext.getContainerName())
                        .orElse(parsed);

                profile.update(assetContext, parsed.getPrivileged(), parsed.getRunAsUser(), parsed.getAllowPrivilegeEscalation(), parsed.getReadOnlyRootFilesystem());
                profiles.add(podProfileStore.save(profile));
            } catch (Exception ignored) {}
        }
        evaluatePolicies(tenant, ResourceType.POD, profiles, context);
    }

    private void processNodes(Tenant tenant, List<NodeProfile> parsedNodes) {
        if (parsedNodes == null) return;
        for (NodeProfile parsed : parsedNodes) {
            try {
                parsed.assignTenant(tenant);
                NodeProfile profile = nodeProfileStore.findByTenantAndName(tenant, parsed.getName())
                        .orElse(parsed);
                profile.update(parsed.getOsImage(), parsed.getKernelVersion(), parsed.getContainerRuntimeVersion(), parsed.getKubeletVersion(), parsed.getCpuCapacity(), parsed.getMemoryCapacity());
                nodeProfileStore.save(profile);
            } catch (Exception ignored) {}
        }
    }

    private void processServices(Tenant tenant, List<ServiceProfile> parsedServices) {
        if (parsedServices == null) return;
        for (ServiceProfile parsed : parsedServices) {
            try {
                parsed.assignTenant(tenant);
                ServiceProfile profile = serviceProfileStore.findByTenantAndNamespaceAndName(tenant, parsed.getNamespace(), parsed.getName())
                        .orElse(parsed);
                profile.update(parsed.getType(), parsed.getClusterIp(), parsed.getExternalIps());
                serviceProfileStore.save(profile);
            } catch (Exception ignored) {}
        }
    }

    private void processDeployments(Tenant tenant, List<DeploymentProfile> parsedDeployments) {
        if (parsedDeployments == null) return;
        for (DeploymentProfile parsed : parsedDeployments) {
            try {
                parsed.assignTenant(tenant);
                DeploymentProfile profile = deploymentProfileStore.findByTenantAndNamespaceAndName(tenant, parsed.getNamespace(), parsed.getName())
                        .orElse(parsed);
                profile.update(parsed.getReplicas(), parsed.getAvailableReplicas(), parsed.getStrategyType(), parsed.getSelectorJson());
                deploymentProfileStore.save(profile);
            } catch (Exception ignored) {}
        }
    }

    private void processNamespaces(Tenant tenant, List<NamespaceProfile> parsedNamespaces) {
        if (parsedNamespaces == null) return;
        for (NamespaceProfile parsed : parsedNamespaces) {
            try {
                parsed.assignTenant(tenant);
                NamespaceProfile profile = namespaceProfileStore.findByTenantAndName(tenant, parsed.getName())
                        .orElse(parsed);
                profile.update(parsed.getStatus());
                namespaceProfileStore.save(profile);
            } catch (Exception ignored) {}
        }
    }

    private void processEvents(Tenant tenant, List<EventProfile> parsedEvents) {
        if (parsedEvents == null) return;
        for (EventProfile parsed : parsedEvents) {
            try {
                parsed.assignTenant(tenant);
                // [해결] Name 필드 대신 고유 식별자인 UID로 조회합니다.
                EventProfile profile = eventProfileStore.findByTenantAndUid(tenant, parsed.getUid())
                        .orElse(parsed);
                profile.update(parsed.getCount(), parsed.getLastTimestamp(), parsed.getMessage());
                eventProfileStore.save(profile);
            } catch (Exception ignored) {}
        }
    }

    private <T> void evaluatePolicies(Tenant tenant, ResourceType type, List<T> resources, SecurityPolicyContext context) {
        for (T resource : resources) {
            List<PolicyEvaluationResult> results = policyEngine.evaluate(type, resource, context);
            for (PolicyEvaluationResult res : results) {
                String name = (resource instanceof PodProfile p) ? p.getAssetContext().getPodName() : "Unknown";
                alertWriter.save(new Alert(tenant, res.getSeverity(), Category.CSPM, res.getMessage(), type.name(), name));
            }
        }
    }
}
