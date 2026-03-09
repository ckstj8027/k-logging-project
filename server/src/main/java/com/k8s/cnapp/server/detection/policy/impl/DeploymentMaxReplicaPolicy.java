package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import org.springframework.stereotype.Component;

@Component
public class DeploymentMaxReplicaPolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.DEPLOYMENT;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.REPLICA_MAX_LIMIT;
    }

    @Override
    public PolicyEvaluationResult evaluate(DeploymentProfile deployment, SecurityPolicyContext context) {
        int maxReplicas = 10;
        try {
            String maxStr = context.getPolicyValue(getSupportedType(), getRuleType());
            if (maxStr != null) {
                maxReplicas = Integer.parseInt(maxStr);
            }
        } catch (NumberFormatException ignored) {}

        if (deployment.getReplicas() != null && deployment.getReplicas() > maxReplicas) {
            return PolicyEvaluationResult.failure(Alert.Severity.LOW, 
                "High replica count detected (" + deployment.getReplicas() + " > " + maxReplicas + ")");
        }
        return PolicyEvaluationResult.success();
    }
}
