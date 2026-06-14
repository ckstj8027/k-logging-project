package com.k8s.cnapp.msa.analysis.policy.impl;

import com.k8s.cnapp.msa.analysis.domain.NodeProfile;
import com.k8s.cnapp.msa.analysis.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;
import org.springframework.stereotype.Component;

@Component
public class NodeMemoryLimitPolicy implements SecurityPolicy<NodeProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.NODE;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.MEMORY_LIMIT_BYTES;
    }

    @Override
    public PolicyEvaluationResult evaluate(NodeProfile node, SecurityPolicyContext context) {
        long memoryLimit = 32L * 1024 * 1024 * 1024;
        try {
            String memStr = context.getPolicyValue(getRuleType());
            if (memStr != null) memoryLimit = Long.parseLong(memStr);
        } catch (NumberFormatException ignored) {}

        long memoryBytes = parseMemory(node.getMemoryCapacity());
        if (memoryBytes > memoryLimit) {
            return PolicyEvaluationResult.failure(Severity.HIGH, 
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
            return Long.parseLong(memStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
