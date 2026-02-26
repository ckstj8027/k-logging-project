package com.k8s.cnapp.server.policy.service;

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

    @PostConstruct
    public void initDefaultPolicies() {
        // 서버 시작 시 기본 정책이 없으면 생성
        createIfNotExists(Policy.ResourceType.POD, Policy.RuleType.PRIVILEGED_DENY, "true", "Disallow privileged containers");
        createIfNotExists(Policy.ResourceType.POD, Policy.RuleType.RUN_AS_ROOT_DENY, "true", "Disallow running as root");
        createIfNotExists(Policy.ResourceType.POD, Policy.RuleType.IMAGE_LATEST_TAG_DENY, "true", "Disallow 'latest' image tag");
        
        createIfNotExists(Policy.ResourceType.SERVICE, Policy.RuleType.PORT_BLACKLIST, "21,22,23,25,53,110,143,389,445,3306,3389,5432,6379,27017", "Blacklisted ports for external exposure");
        createIfNotExists(Policy.ResourceType.SERVICE, Policy.RuleType.EXTERNAL_IP_DENY, "true", "Warn on LoadBalancer/NodePort services");

        createIfNotExists(Policy.ResourceType.DEPLOYMENT, Policy.RuleType.REPLICA_MAX_LIMIT, "3", "Max replica count warning");
        createIfNotExists(Policy.ResourceType.DEPLOYMENT, Policy.RuleType.REPLICA_MIN_LIMIT, "1", "Min replica count warning (0 means down)");

        createIfNotExists(Policy.ResourceType.NODE, Policy.RuleType.CPU_LIMIT_CORES, "1", "Node CPU capacity limit (1cores)");
        createIfNotExists(Policy.ResourceType.NODE, Policy.RuleType.MEMORY_LIMIT_BYTES, "1073741824", "Node Memory capacity limit (1GB)");
    }

    private void createIfNotExists(Policy.ResourceType resourceType, Policy.RuleType ruleType, String value, String description) {
        if (policyRepository.findByResourceTypeAndRuleType(resourceType, ruleType).isEmpty()) {
            policyRepository.save(new Policy(resourceType, ruleType, value, description));
        }
    }

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    @Transactional
    public Policy updatePolicy(Long id, String value, boolean enabled) {
        Policy policy = policyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        policy.update(value, enabled);
        return policy;
    }
    
    public String getPolicyValue(Policy.ResourceType resourceType, Policy.RuleType ruleType) {
        return policyRepository.findByResourceTypeAndRuleType(resourceType, ruleType)
                .filter(Policy::isEnabled)
                .map(Policy::getValue)
                .orElse(null); // 비활성화되거나 없으면 null 반환
    }
}
