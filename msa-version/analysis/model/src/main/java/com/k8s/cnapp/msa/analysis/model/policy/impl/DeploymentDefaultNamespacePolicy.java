package com.k8s.cnapp.msa.analysis.model.policy.impl;

import com.k8s.cnapp.msa.analysis.model.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.model.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;

public class DeploymentDefaultNamespacePolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.DEPLOYMENT;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.DEPLOYMENT_DEFAULT_NAMESPACE_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(DeploymentProfile deployment, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getRuleType()) &&
            "default".equalsIgnoreCase(deployment.getNamespace())) {

            return PolicyEvaluationResult.failure(Severity.LOW,
                String.format("Architectural Risk: Deployment '%s' is in 'default' namespace.",
                deployment.getName()));
        }
        return PolicyEvaluationResult.success();
    }
}
