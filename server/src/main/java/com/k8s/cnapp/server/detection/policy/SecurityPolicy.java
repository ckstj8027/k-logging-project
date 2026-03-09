package com.k8s.cnapp.server.detection.policy;

import com.k8s.cnapp.server.policy.domain.Policy;

/**
 * 보안 정책 인터페이스
 * @param <T> 정책이 적용될 자산(Asset) 타입 (예: PodProfile, ServiceProfile 등)
 */
public interface SecurityPolicy<T> {
    
    /**
     * 정책이 적용되는 리소스 타입을 반환 (Policy Scope 결정용)
     */
    Policy.ResourceType getSupportedType();

    /**
     * 정책의 규칙 타입을 반환
     */
    Policy.RuleType getRuleType();

    /**
     * 정책 검사 실행
     */
    PolicyEvaluationResult evaluate(T target, SecurityPolicyContext context);
}
