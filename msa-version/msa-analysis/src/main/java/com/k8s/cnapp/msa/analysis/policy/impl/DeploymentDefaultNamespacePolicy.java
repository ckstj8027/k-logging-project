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
public class DeploymentDefaultNamespacePolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.DEPLOYMENT;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.NAMESPACE_DEFAULT_DENY;
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
