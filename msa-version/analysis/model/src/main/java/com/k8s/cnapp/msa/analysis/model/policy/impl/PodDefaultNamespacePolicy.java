package com.k8s.cnapp.msa.analysis.model.policy.impl;

import com.k8s.cnapp.msa.analysis.model.PodProfile;
import com.k8s.cnapp.msa.analysis.model.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;

public class PodDefaultNamespacePolicy implements SecurityPolicy<PodProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.POD;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.POD_DEFAULT_NAMESPACE_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(PodProfile pod, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getRuleType()) &&
            "default".equalsIgnoreCase(pod.getAssetContext().getNamespace())) {

            return PolicyEvaluationResult.failure(Severity.LOW,
                String.format("Security Best Practice Violation: Pod '%s' is deployed in the 'default' namespace.",
                pod.getAssetContext().getPodName()));
        }
        return PolicyEvaluationResult.success();
    }
}
