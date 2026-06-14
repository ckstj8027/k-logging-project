package com.k8s.cnapp.msa.analysis.policy.impl;

import com.k8s.cnapp.msa.analysis.domain.PodProfile;
import com.k8s.cnapp.msa.analysis.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;
import org.springframework.stereotype.Component;

@Component
public class PodPrivilegedPolicy implements SecurityPolicy<PodProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.POD;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.PRIVILEGED_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(PodProfile pod, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getRuleType()) && Boolean.TRUE.equals(pod.getPrivileged())) {
            return PolicyEvaluationResult.failure(Severity.CRITICAL, "Privileged container detected");
        }
        return PolicyEvaluationResult.success();
    }
}
