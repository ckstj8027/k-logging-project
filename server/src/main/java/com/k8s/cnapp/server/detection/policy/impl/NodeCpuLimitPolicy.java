package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.NodeProfile;
import org.springframework.stereotype.Component;

@Component
public class NodeCpuLimitPolicy implements SecurityPolicy<NodeProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.NODE;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.CPU_LIMIT_CORES;
    }

    @Override
    public PolicyEvaluationResult evaluate(NodeProfile node, SecurityPolicyContext context) {
        int cpuLimit = 8;
        try {
            String cpuStr = context.getPolicyValue(getSupportedType(), getRuleType());
            if (cpuStr != null) cpuLimit = Integer.parseInt(cpuStr);
        } catch (NumberFormatException ignored) {}

        long cpuCores = parseCpu(node.getCpuCapacity());
        if (cpuCores > cpuLimit) {
            return PolicyEvaluationResult.failure(Alert.Severity.HIGH, 
                "Node CPU capacity exceeds limit (" + cpuCores + " > " + cpuLimit + ")");
        }
        return PolicyEvaluationResult.success();
    }

    private long parseCpu(String cpuStr) {
        if (cpuStr == null) return 0;
        try {
            if (cpuStr.endsWith("m")) {
                return Long.parseLong(cpuStr.substring(0, cpuStr.length() - 1)) / 1000;
            }
            return Long.parseLong(cpuStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
