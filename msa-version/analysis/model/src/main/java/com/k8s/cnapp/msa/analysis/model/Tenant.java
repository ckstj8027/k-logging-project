package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 분석 관점의 테넌트 도메인 모델 (순수 POJO).
 */
@Getter
public class Tenant {

    private final Long id; // MSA 환경에서는 ID를 직접 할당받아 동기화합니다.
    private final String name;
    private final LocalDateTime createdAt;

    public Tenant(Long id, String name) {
        this(id, name, LocalDateTime.now());
    }

    /** 영속성 어댑터가 DB 상태를 복원할 때 사용한다. */
    public Tenant(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }
}
