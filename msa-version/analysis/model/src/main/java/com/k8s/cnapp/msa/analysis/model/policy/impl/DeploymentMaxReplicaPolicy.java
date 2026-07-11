package com.k8s.cnapp.msa.analysis.model.policy.impl;

import com.k8s.cnapp.msa.analysis.model.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.model.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;

public class DeploymentMaxReplicaPolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.DEPLOYMENT;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.DEPLOYMENT_MAX_REPLICAS;
    }

    @Override
    public PolicyEvaluationResult evaluate(DeploymentProfile deployment, SecurityPolicyContext context) {
        int maxReplicas = 10;
        try {
            String maxStr = context.getPolicyValue(getRuleType());
            if (maxStr != null) maxReplicas = Integer.parseInt(maxStr);
        } catch (NumberFormatException ignored) {}

        if (deployment.getReplicas() != null && deployment.getReplicas() > maxReplicas) {
            return PolicyEvaluationResult.failure(Severity.LOW,
                "High replica count detected (" + deployment.getReplicas() + " > " + maxReplicas + ")");
        }
        return PolicyEvaluationResult.success();
    }
}
