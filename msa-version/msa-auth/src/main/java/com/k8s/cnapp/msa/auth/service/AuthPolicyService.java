package com.k8s.cnapp.msa.auth.service;

import com.k8s.cnapp.msa.auth.domain.Policy;
import com.k8s.cnapp.msa.auth.domain.Tenant;
import com.k8s.cnapp.msa.auth.repository.PolicyRepository;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthPolicyService {

    private final PolicyRepository policyRepository;

    @Transactional
    public void createDefaultPoliciesForTenant(Tenant tenant) {
        // Pod Policies
        createIfNotExists(tenant, ResourceType.POD, RuleType.PRIVILEGED_DENY, "true", "Disallow privileged containers");
        createIfNotExists(tenant, ResourceType.POD, RuleType.RUN_AS_ROOT_DENY, "true", "Disallow running as root");
        createIfNotExists(tenant, ResourceType.POD, RuleType.IMAGE_LATEST_TAG_DENY, "true", "Disallow 'latest' image tag");
        
        // Service Policies
        createIfNotExists(tenant, ResourceType.SERVICE, RuleType.PORT_BLACKLIST, "22,3306,5432", "Blacklisted ports");
        createIfNotExists(tenant, ResourceType.SERVICE, RuleType.EXTERNAL_IP_DENY, "true", "Warn on LoadBalancer/NodePort");
    }

    private void createIfNotExists(Tenant tenant, ResourceType resourceType, RuleType ruleType, String value, String description) {
        if (policyRepository.findByTenantAndResourceTypeAndRuleType(tenant, resourceType, ruleType).isEmpty()) {
            policyRepository.save(new Policy(tenant, resourceType, ruleType, value, description));
        }
    }
}
