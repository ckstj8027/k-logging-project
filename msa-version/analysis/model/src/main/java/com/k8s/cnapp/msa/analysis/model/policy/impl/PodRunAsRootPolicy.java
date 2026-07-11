package com.k8s.cnapp.msa.analysis.model.policy.impl;

import com.k8s.cnapp.msa.analysis.model.PodProfile;
import com.k8s.cnapp.msa.analysis.model.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;

public class PodRunAsRootPolicy implements SecurityPolicy<PodProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.POD;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.RUN_AS_ROOT_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(PodProfile pod, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getRuleType()) && Boolean.TRUE.equals(pod.getRunAsRoot())) {
            return PolicyEvaluationResult.failure(Severity.HIGH, "Container running as root detected");
        }
        return PolicyEvaluationResult.success();
    }
}
