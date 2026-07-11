package com.k8s.cnapp.msa.analysis.model;

import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import lombok.Getter;

/**
 * 테넌트별 보안 정책 설정 도메인 모델 (순수 POJO).
 */
@Getter
public class Policy {

    private final Long id;
    private final ResourceType resourceType;
    private final RuleType ruleType;
    private final String value;
    private final boolean enabled;
    private final String description;

    public Policy(Long id, ResourceType resourceType, RuleType ruleType, String value, boolean enabled, String description) {
        this.id = id;
        this.resourceType = resourceType;
        this.ruleType = ruleType;
        this.value = value;
        this.enabled = enabled;
        this.description = description;
    }
}
