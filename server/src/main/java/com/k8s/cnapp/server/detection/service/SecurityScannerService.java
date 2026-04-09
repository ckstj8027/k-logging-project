package com.k8s.cnapp.server.detection.service;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.detection.event.RabbitSecurityEventPublisher;
import com.k8s.cnapp.server.detection.event.ScanRequestEvent;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.policy.service.PolicyService;
import com.k8s.cnapp.server.profile.domain.*;
import com.k8s.cnapp.server.profile.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityScannerService {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final NamespaceProfileRepository namespaceProfileRepository;
    private final EventProfileRepository eventProfileRepository;
    private final AlertRepository alertRepository;
    private final PolicyService policyService;
    private final TenantRepository tenantRepository;
    private final PolicyEngine policyEngine;
    private final Executor scanExecutor;

    /**
     * RabbitMQ 메시지 리스너: 실시간 정밀 스캔 (변경된 리소스만 타겟팅)
     */
    @RabbitListener(queues = RabbitSecurityEventPublisher.SCAN_QUEUE)
    public void onScanRequest(ScanRequestEvent event) {
        log.info("Received targeted scan request for tenant ID: {}", event.getTenantId());
        tenantRepository.findById(event.getTenantId()).ifPresent(tenant -> {
            try {
                if (event.isTargetedScan()) {
                    performTargetedScan(tenant, event.getUpdatedResourceIds());
                } else {
                    scanForTenant(tenant); // ID가 없으면 전체 스캔
                }
            } catch (Exception e) {
                log.error("Error during real-time scan", e);
            }
        });
    }

    /**
     * 정밀 스캔 로직: 변경된 ID 리스트만 DB에서 다시 조회하여 스캔
     */
    private void performTargetedScan(Tenant tenant, Map<Policy.ResourceType, List<Long>> idMap) {
        log.info("Starting pinpoint scan for tenant: {} (Resource Types: {})", tenant.getName(), idMap.keySet());
        
        SecurityPolicyContext context = new SecurityPolicyContext(tenant, policyService);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 1. Pod 정밀 스캔 (비동기)
        if (idMap.containsKey(Policy.ResourceType.POD)) {
            futures.add(CompletableFuture.runAsync(() -> {
                List<Long> ids = idMap.get(Policy.ResourceType.POD);
                List<PodProfile> targets = podProfileRepository.findAllById(ids);
                log.info("Loaded {} pods out of {} requested IDs for pinpoint scan.", targets.size(), ids.size());
                
                List<Alert> newAlerts = new ArrayList<>();
                Map<String, Alert> alertMap = loadOpenAlerts(tenant);
                evaluatePolicies(tenant, Policy.ResourceType.POD, targets, alertMap, newAlerts, context);
                saveNewAlerts(tenant, newAlerts);
            }, scanExecutor));
        }

        // 2. Service 정밀 스캔 (비동기)
        if (idMap.containsKey(Policy.ResourceType.SERVICE)) {
            futures.add(CompletableFuture.runAsync(() -> {
                List<Long> ids = idMap.get(Policy.ResourceType.SERVICE);
                List<ServiceProfile> targets = serviceProfileRepository.findAllByIdWithPorts(ids);
                log.info("Loaded {} services out of {} requested IDs for pinpoint scan.", targets.size(), ids.size());
                
                List<Alert> newAlerts = new ArrayList<>();
                Map<String, Alert> alertMap = loadOpenAlerts(tenant);
                evaluatePolicies(tenant, Policy.ResourceType.SERVICE, targets, alertMap, newAlerts, context);
                saveNewAlerts(tenant, newAlerts);
            }, scanExecutor));
        }

        // 3. Node 정밀 스캔 (비동기)
        if (idMap.containsKey(Policy.ResourceType.NODE)) {
            futures.add(CompletableFuture.runAsync(() -> {
                List<Long> ids = idMap.get(Policy.ResourceType.NODE);
                List<NodeProfile> targets = nodeProfileRepository.findAllById(ids);
                log.info("Loaded {} nodes out of {} requested IDs for pinpoint scan.", targets.size(), ids.size());
                
                List<Alert> newAlerts = new ArrayList<>();
                Map<String, Alert> alertMap = loadOpenAlerts(tenant);
                evaluatePolicies(tenant, Policy.ResourceType.NODE, targets, alertMap, newAlerts, context);
                saveNewAlerts(tenant, newAlerts);
            }, scanExecutor));
        }

        // 4. Deployment 정밀 스캔 (비동기)
        if (idMap.containsKey(Policy.ResourceType.DEPLOYMENT)) {
            futures.add(CompletableFuture.runAsync(() -> {
                List<Long> ids = idMap.get(Policy.ResourceType.DEPLOYMENT);
                List<DeploymentProfile> targets = deploymentProfileRepository.findAllById(ids);
                log.info("Loaded {} deployments out of {} requested IDs for pinpoint scan.", targets.size(), ids.size());
                
                List<Alert> newAlerts = new ArrayList<>();
                Map<String, Alert> alertMap = loadOpenAlerts(tenant);
                evaluatePolicies(tenant, Policy.ResourceType.DEPLOYMENT, targets, alertMap, newAlerts, context);
                saveNewAlerts(tenant, newAlerts);
            }, scanExecutor));
        }

        // 모든 비동기 작업이 완료될 때까지 대기하여 MQ 메시지 처리 보장
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Completed pinpoint scan for tenant: {}", tenant.getName());
    }

    @Scheduled(fixedRate = 3600000) // 정기 보장 스캔 (1시간 단위)
    @SchedulerLock(name = "SecurityScannerService_scan", lockAtMostFor = "15m", lockAtLeastFor = "5m")
    @Transactional
    public void scan() {
        log.info("Starting periodic full safety scan...");
        tenantRepository.findAll().forEach(this::scanForTenant);
    }

    private void scanForTenant(Tenant tenant) {
        Map<String, Alert> alertMap = loadOpenAlerts(tenant);
        List<Alert> newAlerts = new ArrayList<>();
        SecurityPolicyContext context = new SecurityPolicyContext(tenant, policyService);

        evaluatePolicies(tenant, Policy.ResourceType.POD, podProfileRepository.findAllByTenant(tenant), alertMap, newAlerts, context);
        evaluatePolicies(tenant, Policy.ResourceType.SERVICE, serviceProfileRepository.findAllByTenantWithPorts(tenant), alertMap, newAlerts, context);
        evaluatePolicies(tenant, Policy.ResourceType.DEPLOYMENT, deploymentProfileRepository.findAllByTenant(tenant), alertMap, newAlerts, context);
        evaluatePolicies(tenant, Policy.ResourceType.NODE, nodeProfileRepository.findAllByTenant(tenant), alertMap, newAlerts, context);

        saveNewAlerts(tenant, newAlerts);
    }

    private Map<String, Alert> loadOpenAlerts(Tenant tenant) {
        return alertRepository.findByStatus(Alert.Status.OPEN).stream()
                .filter(a -> a.getTenant().getId().equals(tenant.getId()))
                .collect(Collectors.toMap(
                        a -> a.getResourceType() + "/" + a.getResourceName() + "/" + a.getMessage(),
                        Function.identity(), (e, r) -> e
                ));
    }

    private void saveNewAlerts(Tenant tenant, List<Alert> newAlerts) {
        if (!newAlerts.isEmpty()) {
            try {
                alertRepository.saveAll(newAlerts);
                log.info("Tenant {}: Generated {} new alerts.", tenant.getName(), newAlerts.size());
            } catch (DataIntegrityViolationException e) {
                log.info("Tenant {}: Duplicate alerts detected and skipped (Database Unique Constraint).", tenant.getName());
            }
        }
    }

    private <T> void evaluatePolicies(Tenant tenant, Policy.ResourceType type, List<T> resources, 
                                      Map<String, Alert> alertMap, List<Alert> newAlerts, SecurityPolicyContext context) {
        for (T resource : resources) {
            String resourceName = getResourceName(resource);
            List<PolicyEvaluationResult> results = policyEngine.evaluate(type, resource, context);
            for (PolicyEvaluationResult result : results) {
                addAlertIfNew(tenant, alertMap, newAlerts, result.getSeverity(), Alert.Category.CSPM,
                        result.getMessage(), type.name(), resourceName);
            }
        }
    }

    private String getResourceName(Object resource) {
        if (resource instanceof PodProfile p) return p.getAssetContext().getAssetKey();
        if (resource instanceof ServiceProfile s) return s.getNamespace() + "/" + s.getName();
        if (resource instanceof DeploymentProfile d) return d.getNamespace() + "/" + d.getName();
        if (resource instanceof NodeProfile n) return n.getName();
        return "Unknown";
    }

    private void addAlertIfNew(Tenant tenant, Map<String, Alert> alertMap, List<Alert> newAlerts, 
                               Alert.Severity severity, Alert.Category category, 
                               String message, String resourceType, String resourceName) {
        String key = resourceType + "/" + resourceName + "/" + message;
        if (!alertMap.containsKey(key)) {
            Alert alert = new Alert(tenant, severity, category, message, resourceType, resourceName);
            newAlerts.add(alert);
            alertMap.put(key, alert);
            log.warn("[ALERT] [{}] {} - {} ({})", severity, category, message, resourceName);
        }
    }
}
