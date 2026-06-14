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
public class DeploymentMaxReplicaPolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.DEPLOYMENT;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.REPLICA_MAX_LIMIT;
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
