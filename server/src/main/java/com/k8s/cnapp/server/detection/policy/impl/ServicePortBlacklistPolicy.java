package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.ServicePortProfile;
import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ServicePortBlacklistPolicy implements SecurityPolicy<ServiceProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.SERVICE;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.PORT_BLACKLIST;
    }

    @Override
    public PolicyEvaluationResult evaluate(ServiceProfile service, SecurityPolicyContext context) {
        // External IP가 아니어도 Port 검사를 하는지 기존 로직 확인:
        // 기존엔 External IP Deny 안에서 Port 검사를 했지만, 정책이 분리되었으므로 독립적으로 검사
        String portBlacklistStr = context.getPolicyValue(getSupportedType(), getRuleType());
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
                return PolicyEvaluationResult.failure(Alert.Severity.HIGH, 
                    "Dangerous port exposed: " + port.getPort() + " (" + port.getProtocol() + ")");
            }
        }

        return PolicyEvaluationResult.success();
    }
}
