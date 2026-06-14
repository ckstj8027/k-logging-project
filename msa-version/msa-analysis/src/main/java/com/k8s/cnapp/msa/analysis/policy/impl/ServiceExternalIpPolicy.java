package com.k8s.cnapp.msa.analysis.policy.impl;

import com.k8s.cnapp.msa.analysis.domain.ServiceProfile;
import com.k8s.cnapp.msa.analysis.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;
import org.springframework.stereotype.Component;

@Component
public class ServiceExternalIpPolicy implements SecurityPolicy<ServiceProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.SERVICE;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.EXTERNAL_IP_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(ServiceProfile service, SecurityPolicyContext context) {
        if (context.isPolicyEnabled(getRuleType()) && 
            ("LoadBalancer".equals(service.getType()) || "NodePort".equals(service.getType()))) {
            return PolicyEvaluationResult.failure(Severity.MEDIUM, "Service exposed externally (" + service.getType() + ")");
        }
        return PolicyEvaluationResult.success();
    }
}
