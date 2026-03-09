package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import org.springframework.stereotype.Component;

@Component
public class ServiceExternalIpPolicy implements SecurityPolicy<ServiceProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.SERVICE;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.EXTERNAL_IP_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(ServiceProfile service, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getSupportedType(), getRuleType()) && 
            ("LoadBalancer".equals(service.getType()) || "NodePort".equals(service.getType()))) {
            return PolicyEvaluationResult.failure(Alert.Severity.MEDIUM, "Service exposed externally (" + service.getType() + ")");
        }
        return PolicyEvaluationResult.success();
    }
}
