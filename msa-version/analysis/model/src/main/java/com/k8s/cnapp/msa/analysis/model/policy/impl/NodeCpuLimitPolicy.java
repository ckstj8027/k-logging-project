package com.k8s.cnapp.msa.analysis.model.policy.impl;

import com.k8s.cnapp.msa.analysis.model.NodeProfile;
import com.k8s.cnapp.msa.analysis.model.policy.PolicyEvaluationResult;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicyContext;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.common.model.Severity;

public class NodeCpuLimitPolicy implements SecurityPolicy<NodeProfile> {

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.NODE;
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.NODE_CPU_LIMIT;
    }

    @Override
    public PolicyEvaluationResult evaluate(NodeProfile node, SecurityPolicyContext context) {
        int cpuLimit = 8;
        try {
            String cpuStr = context.getPolicyValue(getRuleType());
            if (cpuStr != null) cpuLimit = Integer.parseInt(cpuStr);
        } catch (NumberFormatException ignored) {}

        long cpuCores = parseCpu(node.getCpuCapacity());
        if (cpuCores > cpuLimit) {
            return PolicyEvaluationResult.failure(Severity.HIGH,
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
