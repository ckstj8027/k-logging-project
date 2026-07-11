package com.k8s.cnapp.msa.analysis.model.policy.impl;

import com.k8s.cnapp.msa.analysis.model.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.model.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;

public class DeploymentMinReplicaPolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.DEPLOYMENT;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.DEPLOYMENT_MIN_REPLICAS;
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
