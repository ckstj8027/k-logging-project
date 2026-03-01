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
        createIfNotExists(tenant, Policy.ResourceType.POD, Policy.RuleType.PRIVILEGED_DENY, "true", "Disallow privileged containers");
        createIfNotExists(tenant, Policy.ResourceType.POD, Policy.RuleType.RUN_AS_ROOT_DENY, "true", "Disallow running as root");
        createIfNotExists(tenant, Policy.ResourceType.POD, Policy.RuleType.IMAGE_LATEST_TAG_DENY, "true", "Disallow 'latest' image tag");
        
        createIfNotExists(tenant, Policy.ResourceType.SERVICE, Policy.RuleType.PORT_BLACKLIST, "21,22,23,25,53,110,143,389,445,3306,3389,5432,6379,27017", "Blacklisted ports for external exposure");
        createIfNotExists(tenant, Policy.ResourceType.SERVICE, Policy.RuleType.EXTERNAL_IP_DENY, "true", "Warn on LoadBalancer/NodePort services");

        createIfNotExists(tenant, Policy.ResourceType.DEPLOYMENT, Policy.RuleType.REPLICA_MAX_LIMIT, "10", "Max replica count warning");
        createIfNotExists(tenant, Policy.ResourceType.DEPLOYMENT, Policy.RuleType.REPLICA_MIN_LIMIT, "1", "Min replica count warning (0 means down)");

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

    @Transactional
    public Policy updatePolicy(Long id, String value, boolean enabled) {
        // 본인 Tenant의 정책인지 확인 필요 (여기서는 간단히 ID로 조회하지만, 실제로는 Tenant 검증 로직 추가 권장)
        Policy policy = policyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        
        // Tenant 검증
        Tenant currentTenant = authService.getCurrentTenant();
        if (!policy.getTenant().getId().equals(currentTenant.getId())) {
             throw new SecurityException("Access denied to this policy");
        }

        policy.update(value, enabled);
        return policy;
    }
    
    public String getPolicyValue(Policy.ResourceType resourceType, Policy.RuleType ruleType) {
        // 현재 로그인한 사용자의 Tenant 정책 값 조회 (스캐너 등 내부 로직용)
        // 주의: SecurityScannerService는 스케줄러에 의해 실행되므로 SecurityContext가 없을 수 있음.
        // 따라서 스캐너에서는 Tenant를 명시적으로 넘겨주거나, 모든 Tenant를 순회하며 검사해야 함.
        // 여기서는 일단 SecurityContext가 있다고 가정 (Controller 호출 시)
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
