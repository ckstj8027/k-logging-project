package com.k8s.cnapp.msa.analysis.policy.impl;

import com.k8s.cnapp.msa.analysis.domain.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;
import org.springframework.stereotype.Component;

@Component
public class DeploymentMinReplicaPolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.DEPLOYMENT;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.REPLICA_MIN_LIMIT;
    }

    @Override
    public PolicyEvaluationResult evaluate(DeploymentProfile deployment, SecurityPolicyContext context) {
        int minReplicas = 1;
        try {
            String minStr = context.getPolicyValue(getRuleType());
            if (minStr != null) minReplicas = Integer.parseInt(minStr);
        } catch (NumberFormatException ignored) {}

        if (deployment.getReplicas() != null && deployment.getReplicas() < minReplicas) {
            return PolicyEvaluationResult.failure(Severity.MEDIUM, 
                "Low replica count detected (" + deployment.getReplicas() + " < " + minReplicas + ")");
        }
        return PolicyEvaluationResult.success();
    }
}
