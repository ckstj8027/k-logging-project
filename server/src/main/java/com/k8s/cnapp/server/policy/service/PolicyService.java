package com.k8s.cnapp.server.policy.service;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.auth.service.AuthService;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.policy.repository.PolicyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final TenantRepository tenantRepository;
    private final AuthService authService;

    @PostConstruct
    @Transactional
    public void initDefaultPolicies() {
        // Default Tenant에 대한 기본 정책 생성
        tenantRepository.findByName("Default Company").ifPresent(this::createDefaultPoliciesForTenant);
    }

    public void createDefaultPoliciesForTenant(Tenant tenant) {
        // Pod Policies
        createIfNotExists(tenant, Policy.ResourceType.POD, Policy.RuleType.PRIVILEGED_DENY, "true", "Disallow privileged containers");
        createIfNotExists(tenant, Policy.ResourceType.POD, Policy.RuleType.RUN_AS_ROOT_DENY, "true", "Disallow running as root");
        createIfNotExists(tenant, Policy.ResourceType.POD, Policy.RuleType.IMAGE_LATEST_TAG_DENY, "true", "Disallow 'latest' image tag");
        createIfNotExists(tenant, Policy.ResourceType.POD, Policy.RuleType.NAMESPACE_DEFAULT_DENY, "true", "Warn when workloads are deployed in the 'default' namespace");
        
        // Service Policies
        createIfNotExists(tenant, Policy.ResourceType.SERVICE, Policy.RuleType.PORT_BLACKLIST, "21,22,23,25,53,110,143,389,445,3306,3389,5432,6379,27017", "Blacklisted ports for external exposure");
        createIfNotExists(tenant, Policy.ResourceType.SERVICE, Policy.RuleType.EXTERNAL_IP_DENY, "true", "Warn on LoadBalancer/NodePort services");

        // Deployment Policies
        createIfNotExists(tenant, Policy.ResourceType.DEPLOYMENT, Policy.RuleType.REPLICA_MAX_LIMIT, "10", "Max replica count warning");
        createIfNotExists(tenant, Policy.ResourceType.DEPLOYMENT, Policy.RuleType.REPLICA_MIN_LIMIT, "1", "Min replica count warning (0 means down)");
        createIfNotExists(tenant, Policy.ResourceType.DEPLOYMENT, Policy.RuleType.NAMESPACE_DEFAULT_DENY, "true", "Warn when deployments are targetting the 'default' namespace");

        // Node Policies
        createIfNotExists(tenant, Policy.ResourceType.NODE, Policy.RuleType.CPU_LIMIT_CORES, "8", "Node CPU capacity limit (cores)");
        createIfNotExists(tenant, Policy.ResourceType.NODE, Policy.RuleType.MEMORY_LIMIT_BYTES, "34359738368", "Node Memory capacity limit (32GB)");
    }

    private void createIfNotExists(Tenant tenant, Policy.ResourceType resourceType, Policy.RuleType ruleType, String value, String description) {
        if (policyRepository.findByTenantAndResourceTypeAndRuleType(tenant, resourceType, ruleType).isEmpty()) {
            policyRepository.save(new Policy(tenant, resourceType, ruleType, value, description));
        }
    }

    public List<Policy> getAllPolicies() {
        // 현재 로그인한 사용자의 Tenant 정책만 조회
        Tenant tenant = authService.getCurrentTenant();
        return policyRepository.findAllByTenant(tenant);
    }

    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Policy not found"));
    }

    @Transactional
    public Policy updatePolicy(Long id, String value, boolean enabled) {
        Policy policy = getPolicyById(id);
        
        // Tenant 검증
        Tenant currentTenant = authService.getCurrentTenant();
        if (!policy.getTenant().getId().equals(currentTenant.getId())) {
             throw new SecurityException("Access denied to this policy");
        }

        policy.update(value, enabled);
        return policy;
    }
    
    public String getPolicyValue(Policy.ResourceType resourceType, Policy.RuleType ruleType) {
        try {
            Tenant tenant = authService.getCurrentTenant();
            return getPolicyValueForTenant(tenant, resourceType, ruleType);
        } catch (Exception e) {
            return null; // 인증 정보 없음
        }
    }

    // 스캐너 등 내부 서비스용 (Tenant 명시)
    public String getPolicyValueForTenant(Tenant tenant, Policy.ResourceType resourceType, Policy.RuleType ruleType) {
        return policyRepository.findByTenantAndResourceTypeAndRuleType(tenant, resourceType, ruleType)
                .filter(Policy::isEnabled)
                .map(Policy::getValue)
                .orElse(null);
    }
}
