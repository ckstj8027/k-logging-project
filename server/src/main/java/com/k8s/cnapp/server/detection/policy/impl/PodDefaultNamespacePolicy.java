package com.k8s.cnapp.server.detection.policy.impl;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.detection.policy.PolicyEvaluationResult;
import com.k8s.cnapp.server.detection.policy.SecurityPolicy;
import com.k8s.cnapp.server.detection.policy.SecurityPolicyContext;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.profile.domain.PodProfile;
import org.springframework.stereotype.Component;

@Component
public class PodDefaultNamespacePolicy implements SecurityPolicy<PodProfile> {

    @Override
    public Policy.ResourceType getSupportedType() {
        return Policy.ResourceType.POD;
    }

    @Override
    public Policy.RuleType getRuleType() {
        return Policy.RuleType.NAMESPACE_DEFAULT_DENY;
    }

    @Override
    public PolicyEvaluationResult evaluate(PodProfile pod, SecurityPolicyContext context) {
        // DB 정책 설정 확인: 정책이 활성화되어 있고, 네임스페이스가 default일 때만 알림 생성
        if (context.isPolicyEnabled(getSupportedType(), getRuleType()) && 
            "default".equals(pod.getAssetContext().getNamespace())) {
            return PolicyEvaluationResult.failure(Alert.Severity.LOW, "Pod running in default namespace");
        }
        return PolicyEvaluationResult.success();
    }
}
