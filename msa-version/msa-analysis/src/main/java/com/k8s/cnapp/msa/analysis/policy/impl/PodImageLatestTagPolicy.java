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
public class PodImageLatestTagPolicy implements SecurityPolicy<PodProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.POD;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.IMAGE_LATEST_TAG_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(PodProfile pod, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getRuleType()) && 
            pod.getAssetContext().getImage() != null && 
            pod.getAssetContext().getImage().endsWith(":latest")) {
            return PolicyEvaluationResult.failure(Severity.MEDIUM, "Image using 'latest' tag");
        }
        return PolicyEvaluationResult.success();
    }
}
