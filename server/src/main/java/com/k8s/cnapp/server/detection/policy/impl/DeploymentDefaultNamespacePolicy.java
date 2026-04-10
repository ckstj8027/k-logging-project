package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import org.springframework.stereotype.Component;

/**
 * Deployment 레벨에서의 네임스페이스 보안 정책
 */
@Component
public class DeploymentDefaultNamespacePolicy implements SecurityPolicy<DeploymentProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.DEPLOYMENT;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.NAMESPACE_DEFAULT_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(DeploymentProfile deployment, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getSupportedType(), getRuleType()) && 
            "default".equalsIgnoreCase(deployment.getNamespace())) {
            
            return PolicyEvaluationResult.failure(Alert.Severity.LOW, 
                String.format("Architectural Risk: Deployment '%s' is configured for the 'default' namespace. " +
                "It is highly recommended to use logical isolation via custom namespaces.", deployment.getName()));
        }
        return PolicyEvaluationResult.success();
    }
}
