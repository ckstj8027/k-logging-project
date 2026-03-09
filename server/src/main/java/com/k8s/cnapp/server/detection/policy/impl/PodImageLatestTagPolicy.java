package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.PodProfile;
import org.springframework.stereotype.Component;

@Component
public class PodImageLatestTagPolicy implements SecurityPolicy<PodProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.POD;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.IMAGE_LATEST_TAG_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(PodProfile pod, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getSupportedType(), getRuleType()) && 
            pod.getAssetContext().getImage() != null && 
            pod.getAssetContext().getImage().endsWith(":latest")) {
            return PolicyEvaluationResult.failure(Alert.Severity.MEDIUM, "Image using 'latest' tag");
        }
        return PolicyEvaluationResult.success();
    }
}
