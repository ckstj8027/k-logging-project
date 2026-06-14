package com.k8s.cnapp.msa.analysis.policy;

import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;

/**
 * 보안 정책 인터페이스
 * @param <T> 정책이 적용될 자산(Asset) 타입 (예: PodProfile 등)
 */
public interface SecurityPolicy<T> {
    
    /**
     * 정책이 적용되는 리소스 타입을 반환
     */
    ResourceType getSupportedType();

    /**
     * 정책의 규칙 타입을 반환
     */
    RuleType getRuleType();

    /**
     * 정책 검사 실행
     */
    PolicyEvaluationResult evaluate(T target, SecurityPolicyContext context);
}
