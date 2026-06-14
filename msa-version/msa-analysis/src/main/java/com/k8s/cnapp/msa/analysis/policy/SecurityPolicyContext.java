package com.k8s.cnapp.msa.analysis.policy;

import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class SecurityPolicyContext {
    private final Long tenantId;
    private final Map<RuleType, String> activePolicies; // RuleType -> Value

    public String getPolicyValue(RuleType ruleType) {
        return activePolicies.get(ruleType);
    }

    public boolean isPolicyEnabled(RuleType ruleType) {
        String val = getPolicyValue(ruleType);
        return "true".equalsIgnoreCase(val);
    }
}
