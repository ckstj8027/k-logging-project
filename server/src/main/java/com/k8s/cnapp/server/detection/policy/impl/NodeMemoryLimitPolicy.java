package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.NodeProfile;
import org.springframework.stereotype.Component;

@Component
public class NodeMemoryLimitPolicy implements SecurityPolicy<NodeProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.NODE;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.MEMORY_LIMIT_BYTES;
    }

    @Override
    public PolicyEvaluationResult evaluate(NodeProfile node, SecurityPolicyContext context) {
        long memoryLimit = 32L * 1024 * 1024 * 1024;
        try {
            String memStr = context.getPolicyValue(getSupportedType(), getRuleType());
            if (memStr != null) memoryLimit = Long.parseLong(memStr);
        } catch (NumberFormatException ignored) {}

        long memoryBytes = parseMemory(node.getMemoryCapacity());
        if (memoryBytes > memoryLimit) {
            return PolicyEvaluationResult.failure(Alert.Severity.HIGH, 
                "Node Memory capacity exceeds limit (" + (memoryBytes / 1024 / 1024 / 1024) + "GB > " + (memoryLimit / 1024 / 1024 / 1024) + "GB)");
        }
        return PolicyEvaluationResult.success();
    }

    private long parseMemory(String memStr) {
        if (memStr == null) return 0;
        try {
            String lower = memStr.toLowerCase();
            if (lower.endsWith("ki")) return Long.parseLong(memStr.substring(0, memStr.length() - 2)) * 1024;
            if (lower.endsWith("mi")) return Long.parseLong(memStr.substring(0, memStr.length() - 2)) * 1024 * 1024;
            if (lower.endsWith("gi")) return Long.parseLong(memStr.substring(0, memStr.length() - 2)) * 1024 * 1024 * 1024;
            return Long.parseLong(memStr); // bytes
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
