package com.k8s.cnapp.msa.analysis.service;

import com.k8s.cnapp.msa.analysis.domain.*;
import com.k8s.cnapp.msa.analysis.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.analysis.repository.*;
import com.k8s.cnapp.msa.common.dto.ClusterSnapshot;
import com.k8s.cnapp.msa.common.model.Category;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Status;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final TenantRepository tenantRepository;
    private final AlertRepository alertRepository;
    private final PolicyEngine policyEngine;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "alerts:tenant:";

    @Transactional
    public void processSnapshot(Long tenantId, ClusterSnapshot snapshot) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseGet(() -> tenantRepository.save(new Tenant(tenantId, "Tenant-" + tenantId)));

        log.info("Processing complete snapshot for tenant: {}", tenant.getName());

        // 1. Load Policy Context
        SecurityPolicyContext context = new SecurityPolicyContext(tenantId, Map.of(
                RuleType.PRIVILEGED_DENY, "true",
                RuleType.RUN_AS_ROOT_DENY, "true",
                RuleType.IMAGE_LATEST_TAG_DENY, "true",
                RuleType.PORT_BLACKLIST, "22,3306,5432",
                RuleType.EXTERNAL_IP_DENY, "true",
                RuleType.REPLICA_MAX_LIMIT, "10",
                RuleType.CPU_LIMIT_CORES, "8"
        ));

        // 2. 리소스별 업데이트 및 보안 검사 수행
        processPods(tenant, snapshot.pods(), context);
        processServices(tenant, snapshot.services(), context);
        processDeployments(tenant, snapshot.deployments(), context);
        processNodes(tenant, snapshot.nodes(), context);
        
        log.info("Snapshot processing completed for tenant: {}", tenant.getName());
    }

    private void processPods(Tenant tenant, List<V1Pod> k8sPods, SecurityPolicyContext context) {
        if (k8sPods == null) return;
        List<PodProfile> profiles = new ArrayList<>();
        for (V1Pod pod : k8sPods) {
            String ns = pod.getMetadata().getNamespace();
            String name = pod.getMetadata().getName();
            String containerName = (pod.getSpec() != null && !pod.getSpec().getContainers().isEmpty()) 
                                   ? pod.getSpec().getContainers().get(0).getName() : "unknown";

            PodProfile profile = podProfileRepository.findByTenantAndAssetContextNamespaceAndAssetContextPodNameAndAssetContextContainerName(
                    tenant, ns, name, containerName)
                    .orElse(new PodProfile(tenant, 
                        new AssetContext(ns, name, containerName, 
                            (pod.getSpec() != null && !pod.getSpec().getContainers().isEmpty()) ? pod.getSpec().getContainers().get(0).getImage() : null, 
                            null, 
                            (pod.getStatus() != null) ? pod.getStatus().getPhase() : null, 
                            (pod.getStatus() != null) ? pod.getStatus().getPodIP() : null, 
                            (pod.getSpec() != null) ? pod.getSpec().getNodeName() : null), 
                        (pod.getSpec() != null && !pod.getSpec().getContainers().isEmpty() && pod.getSpec().getContainers().get(0).getSecurityContext() != null) ? pod.getSpec().getContainers().get(0).getSecurityContext().getPrivileged() : false, 
                        (pod.getSpec() != null && !pod.getSpec().getContainers().isEmpty() && pod.getSpec().getContainers().get(0).getSecurityContext() != null) ? pod.getSpec().getContainers().get(0).getSecurityContext().getRunAsUser() : null, 
                        (pod.getSpec() != null && !pod.getSpec().getContainers().isEmpty() && pod.getSpec().getContainers().get(0).getSecurityContext() != null) ? pod.getSpec().getContainers().get(0).getSecurityContext().getAllowPrivilegeEscalation() : false, 
                        (pod.getSpec() != null && !pod.getSpec().getContainers().isEmpty() && pod.getSpec().getContainers().get(0).getSecurityContext() != null) ? pod.getSpec().getContainers().get(0).getSecurityContext().getReadOnlyRootFilesystem() : false));
            
            profile.updateLastSeenAt(java.time.LocalDateTime.now());
            profiles.add(podProfileRepository.save(profile));
        }
        evaluatePolicies(tenant, ResourceType.POD, profiles, context);
    }

    private void processServices(Tenant tenant, List<V1Service> k8sServices, SecurityPolicyContext context) {
        if (k8sServices == null) return;
        List<ServiceProfile> profiles = new ArrayList<>();
        for (V1Service svc : k8sServices) {
            ServiceProfile profile = serviceProfileRepository.findByTenantAndNamespaceAndName(
                    tenant, svc.getMetadata().getNamespace(), svc.getMetadata().getName())
                    .orElse(new ServiceProfile(tenant, svc.getMetadata().getNamespace(), svc.getMetadata().getName(), 
                        svc.getSpec() != null ? svc.getSpec().getType() : "ClusterIP", 
                        svc.getSpec() != null ? svc.getSpec().getClusterIP() : null, null));
            
            profile.updateLastSeenAt(java.time.LocalDateTime.now());
            profiles.add(serviceProfileRepository.save(profile));
        }
        evaluatePolicies(tenant, ResourceType.SERVICE, profiles, context);
    }

    private void processDeployments(Tenant tenant, List<V1Deployment> k8sDeployments, SecurityPolicyContext context) {
        if (k8sDeployments == null) return;
        List<DeploymentProfile> profiles = new ArrayList<>();
        for (V1Deployment deploy : k8sDeployments) {
            DeploymentProfile profile = deploymentProfileRepository.findByTenantAndNamespaceAndName(
                    tenant, deploy.getMetadata().getNamespace(), deploy.getMetadata().getName())
                    .orElse(new DeploymentProfile(tenant, deploy.getMetadata().getNamespace(), deploy.getMetadata().getName(), 
                        deploy.getSpec() != null ? deploy.getSpec().getReplicas() : 1, 
                        (deploy.getStatus() != null) ? deploy.getStatus().getAvailableReplicas() : 0, 
                        (deploy.getSpec() != null && deploy.getSpec().getStrategy() != null) ? deploy.getSpec().getStrategy().getType() : "RollingUpdate", null));
            
            profile.updateLastSeenAt(java.time.LocalDateTime.now());
            profiles.add(deploymentProfileRepository.save(profile));
        }
        evaluatePolicies(tenant, ResourceType.DEPLOYMENT, profiles, context);
    }

    private void processNodes(Tenant tenant, List<V1Node> k8sNodes, SecurityPolicyContext context) {
        if (k8sNodes == null) return;
        List<NodeProfile> profiles = new ArrayList<>();
        for (V1Node node : k8sNodes) {
            NodeProfile profile = nodeProfileRepository.findByTenantAndName(tenant, node.getMetadata().getName())
                    .orElse(new NodeProfile(tenant, node.getMetadata().getName(), 
                        (node.getStatus() != null && node.getStatus().getNodeInfo() != null) ? node.getStatus().getNodeInfo().getOsImage() : "unknown", 
                        (node.getStatus() != null && node.getStatus().getNodeInfo() != null) ? node.getStatus().getNodeInfo().getKernelVersion() : "unknown", 
                        (node.getStatus() != null && node.getStatus().getNodeInfo() != null) ? node.getStatus().getNodeInfo().getContainerRuntimeVersion() : "unknown", 
                        (node.getStatus() != null && node.getStatus().getNodeInfo() != null) ? node.getStatus().getNodeInfo().getKubeletVersion() : "unknown", 
                        (node.getStatus() != null && node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("cpu") != null) ? node.getStatus().getCapacity().get("cpu").toSuffixedString() : "0", 
                        (node.getStatus() != null && node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("memory") != null) ? node.getStatus().getCapacity().get("memory").toSuffixedString() : "0"));
            
            profile.updateLastSeenAt(java.time.LocalDateTime.now());
            profiles.add(nodeProfileRepository.save(profile));
        }
        evaluatePolicies(tenant, ResourceType.NODE, profiles, context);
    }

    private <T> void evaluatePolicies(Tenant tenant, ResourceType type, List<T> resources, SecurityPolicyContext context) {
        Map<String, Alert> openAlerts = loadOpenAlerts(tenant);
        
        for (T resource : resources) {
            List<PolicyEvaluationResult> results = policyEngine.evaluate(type, resource, context);
            for (PolicyEvaluationResult result : results) {
                String resourceName = getResourceName(resource);
                addAlertIfNew(tenant, openAlerts, result, type.name(), resourceName);
            }
        }
    }

    private Map<String, Alert> loadOpenAlerts(Tenant tenant) {
        return alertRepository.findByTenantAndStatus(tenant, Status.OPEN).stream()
                .collect(Collectors.toMap(
                        a -> a.getResourceType() + "/" + a.getResourceName() + "/" + a.getMessage(),
                        a -> a, (e, r) -> e
                ));
    }

    private void addAlertIfNew(Tenant tenant, Map<String, Alert> openAlerts, PolicyEvaluationResult result, String type, String name) {
        String key = type + "/" + name + "/" + result.getMessage();
        if (!openAlerts.containsKey(key)) {
            Alert alert = new Alert(tenant, result.getSeverity(), Category.CSPM, result.getMessage(), type, name);
            alertRepository.save(alert);
            
            // 캐시 무효화 (Invalidation) 로직
            String cacheKey = CACHE_KEY_PREFIX + tenant.getId();
            redisTemplate.delete(cacheKey);
            
            log.warn("[NEW ALERT & CACHE EVICT] {} - {}", name, result.getMessage());
        }
    }

    private String getResourceName(Object resource) {
        if (resource instanceof PodProfile p) return p.getAssetContext().getAssetKey();
        if (resource instanceof ServiceProfile s) return s.getNamespace() + "/" + s.getName();
        if (resource instanceof DeploymentProfile d) return d.getNamespace() + "/" + d.getName();
        if (resource instanceof NodeProfile n) return n.getName();
        return "Unknown";
    }
}
