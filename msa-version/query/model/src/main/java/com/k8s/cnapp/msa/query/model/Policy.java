package com.k8s.cnapp.msa.query.model;

import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 조회 관점의 정책 도메인 모델 (순수 POJO).
 * 기존 JPA 엔티티와 동일한 JSON 형태(프로퍼티명/타입)를 유지한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Policy {
    private Long id;
    private Long tenantId;
    private ResourceType resourceType;
    private RuleType ruleType;
    private String value;
    private boolean enabled;

    public void update(String value, boolean enabled) {
        this.value = value;
        this.enabled = enabled;
    }
}
