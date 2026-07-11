package com.k8s.cnapp.msa.auth.model;

import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;

/**
 * 정책 도메인 모델 (순수 POJO).
 */
public record Policy(Long id, Long tenantId, ResourceType resourceType, RuleType ruleType,
                     String value, String description, boolean enabled) {

    /** 기존 Policy 생성자와 동일하게 enabled = true 로 생성한다. */
    public static Policy create(Long tenantId, ResourceType resourceType, RuleType ruleType,
                                String value, String description) {
        return new Policy(null, tenantId, resourceType, ruleType, value, description, true);
    }
}
