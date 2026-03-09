package com.k8s.cnapp.server.detection.policy;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.policy.service.PolicyService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SecurityPolicyContext {
    private final Tenant tenant;
    private final PolicyService policyService;

    public String getPolicyValue(Policy.ResourceType resourceType, Policy.RuleType ruleType) {
        return policyService.getPolicyValueForTenant(tenant, resourceType, ruleType);
    }

    public boolean isPolicyEnabled(Policy.ResourceType resourceType, Policy.RuleType ruleType) {
        String val = getPolicyValue(resourceType, ruleType);
        return "true".equalsIgnoreCase(val);
    }
}
