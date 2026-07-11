package com.k8s.cnapp.msa.query.model;

import com.k8s.cnapp.msa.common.model.Category;
import com.k8s.cnapp.msa.common.model.Severity;
import com.k8s.cnapp.msa.common.model.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 조회 관점의 알림 읽기 모델 (순수 POJO).
 * 기존 JPA 엔티티와 동일한 JSON 형태(프로퍼티명/타입)를 유지한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Alert {
    private Long id;
    private Long tenantId;
    private Severity severity;
    private Category category;
    private String message;
    private String resourceType;
    private String resourceName;
    private Status status;
    private LocalDateTime createdAt;
}
