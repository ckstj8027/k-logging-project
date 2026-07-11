package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 자산 프로파일 공통 도메인 모델 (순수 POJO).
 * 기존 JPA MappedSuperclass 와 동일한 필드/의미를 유지한다.
 */
@Getter
public abstract class BaseResourceProfile {

    protected Long id;
    protected Tenant tenant;
    protected LocalDateTime createdAt;
    protected LocalDateTime lastSeenAt;

    protected BaseResourceProfile() {
    }

    protected BaseResourceProfile(Tenant tenant) {
        this.tenant = tenant;
    }

    /** 파싱 시점에는 소유 테넌트를 알 수 없으므로 유스케이스에서 지정한다. */
    public void assignTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public void updateLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    /** 영속성 어댑터가 DB 상태를 복원할 때 사용한다. */
    protected void restoreBase(Long id, Tenant tenant, LocalDateTime createdAt, LocalDateTime lastSeenAt) {
        this.id = id;
        this.tenant = tenant;
        this.createdAt = createdAt;
        this.lastSeenAt = lastSeenAt;
    }
}
