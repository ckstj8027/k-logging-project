package com.k8s.cnapp.msa.analysis.policy.impl;

import com.k8s.cnapp.msa.analysis.domain.ServicePortProfile;
import com.k8s.cnapp.msa.analysis.domain.ServiceProfile;
import com.k8s.cnapp.msa.analysis.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ServicePortBlacklistPolicy implements SecurityPolicy<ServiceProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.SERVICE;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.PORT_BLACKLIST;
    }

    @Override
    public PolicyEvaluationResult evaluate(ServiceProfile service, SecurityPolicyContext context) {
        String portBlacklistStr = context.getPolicyValue(getRuleType());
        if (portBlacklistStr == null || portBlacklistStr.isEmpty() || service.getPorts() == null) {
            return PolicyEvaluationResult.success();
        }

        Set<Integer> dangerousPorts = new HashSet<>();
        for (String p : portBlacklistStr.split(",")) {
            try {
                dangerousPorts.add(Integer.parseInt(p.trim()));
            } catch (NumberFormatException ignored) {}
        }

        for (ServicePortProfile port : service.getPorts()) {
            if (port.getPort() != null && dangerousPorts.contains(port.getPort())) {
                return PolicyEvaluationResult.failure(Severity.HIGH, 
                    "Dangerous port exposed: " + port.getPort() + " (" + port.getProtocol() + ")");
            }
        }

        return PolicyEvaluationResult.success();
    }
}
