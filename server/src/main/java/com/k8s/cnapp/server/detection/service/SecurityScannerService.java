package com.k8s.cnapp.server.detection.service;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.policy.service.PolicyService;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.profile.domain.*;
import com.k8s.cnapp.server.profile.repository.DeploymentProfileRepository;
import com.k8s.cnapp.server.profile.repository.NodeProfileRepository;
import com.k8s.cnapp.server.profile.repository.PodProfileRepository;
import com.k8s.cnapp.server.profile.repository.ServiceProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final AlertRepository alertRepository;
    private final PolicyService policyService;
    private final TenantRepository tenantRepository;
    private final PolicyEngine policyEngine;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scan() {
        log.info("Security Scan: Executing with Policy Engine (Optimized for Scope)...");
        try {
            List<Tenant> tenants = tenantRepository.findAll();
            for (Tenant tenant : tenants) {
                scanForTenant(tenant);
            }
        } catch (Exception e) {
            log.error("Critical error during security scan", e);
        }
    }

    private void scanForTenant(Tenant tenant) {
        // 기존 Alert 로드 (신규 중복 방지용)
        List<Alert> openAlerts = alertRepository.findByStatus(Alert.Status.OPEN).stream()
                .filter(a -> a.getTenant().getId().equals(tenant.getId()))
                .toList();
        
        Map<String, Alert> alertMap = openAlerts.stream()
                .collect(Collectors.toMap(
                        a -> a.getResourceType() + "/" + a.getResourceName() + "/" + a.getMessage(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<Alert> newAlerts = new ArrayList<>();
        SecurityPolicyContext context = new SecurityPolicyContext(tenant, policyService);

        // --- Policy Scope 기반 스캔 (타입별 정책만 선별 실행) ---
        
        // 1. Pod 스캔 (Policy.ResourceType.POD 정책들만 실행)
        evaluatePolicies(tenant, Policy.ResourceType.POD, podProfileRepository.findAllByTenant(tenant), alertMap, newAlerts, context);
        
        // 2. Service 스캔 (Policy.ResourceType.SERVICE 정책들만 실행)
        evaluatePolicies(tenant, Policy.ResourceType.SERVICE, serviceProfileRepository.findAllByTenantWithPorts(tenant), alertMap, newAlerts, context);
        
        // 3. Deployment 스캔
        evaluatePolicies(tenant, Policy.ResourceType.DEPLOYMENT, deploymentProfileRepository.findAllByTenant(tenant), alertMap, newAlerts, context);
        
        // 4. Node 스캔
        evaluatePolicies(tenant, Policy.ResourceType.NODE, nodeProfileRepository.findAllByTenant(tenant), alertMap, newAlerts, context);

        if (!newAlerts.isEmpty()) {
            alertRepository.saveAll(newAlerts);
            log.info("Tenant {}: Generated {} new alerts.", tenant.getName(), newAlerts.size());
        }
    }

    /**
     * 특정 리소스 타입의 자산들에 대해 Policy Engine을 통해 정책 평가를 수행
     */
    private <T> void evaluatePolicies(Tenant tenant, Policy.ResourceType type, List<T> resources, 
                                      Map<String, Alert> alertMap, List<Alert> newAlerts, SecurityPolicyContext context) {
        for (T resource : resources) {
            String resourceName = getResourceName(resource);
            
            // Policy Engine이 내부적으로 ResourceType에 매핑된 정책들만 초고속으로 추출하여 실행
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
