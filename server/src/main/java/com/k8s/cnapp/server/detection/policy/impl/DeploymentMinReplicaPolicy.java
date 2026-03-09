package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import org.springframework.stereotype.Component;

@Component
public class DeploymentMinReplicaPolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.DEPLOYMENT;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.REPLICA_MIN_LIMIT;
    }

    @Override
    public PolicyEvaluationResult evaluate(DeploymentProfile deployment, SecurityPolicyContext context) {
        int minReplicas = 1;
        try {
            String minStr = context.getPolicyValue(getSupportedType(), getRuleType());
            if (minStr != null) {
                minReplicas = Integer.parseInt(minStr);
            }
        } catch (NumberFormatException ignored) {}

        if (deployment.getReplicas() != null && deployment.getReplicas() < minReplicas) {
            return PolicyEvaluationResult.failure(Alert.Severity.MEDIUM, 
                "Low replica count detected (" + deployment.getReplicas() + " < " + minReplicas + ")");
        }
        return PolicyEvaluationResult.success();
    }
}
