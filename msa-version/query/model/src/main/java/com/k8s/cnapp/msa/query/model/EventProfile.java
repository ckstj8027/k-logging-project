package com.k8s.cnapp.msa.query.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 조회 관점의 Event 자산 읽기 모델 (순수 POJO).
 * 기존 JPA 엔티티와 동일한 JSON 형태(프로퍼티명/타입)를 유지한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EventProfile {
    private Long id;
    private Long tenantId;
    private String namespace;
    private String involvedObjectKind;
    private String involvedObjectName;
    private String reason;
    private String message;
    private String type;
    private Integer count;
    private OffsetDateTime lastTimestamp;
    private String uid;
    private LocalDateTime lastSeenAt;
}
