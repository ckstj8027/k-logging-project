package com.k8s.cnapp.msa.query.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 조회 관점의 Deployment 자산 읽기 모델 (순수 POJO).
 * 기존 JPA 엔티티와 동일한 JSON 형태(프로퍼티명/타입)를 유지한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeploymentProfile {
    private Long id;
    private Long tenantId;
    private String namespace;
    private String name;
    private Integer replicas;
    private Integer availableReplicas;
    private String strategyType;
    private String selectorJson;
    private LocalDateTime lastSeenAt;
}
