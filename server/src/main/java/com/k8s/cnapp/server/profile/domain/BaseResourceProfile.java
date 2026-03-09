package com.k8s.cnapp.server.profile.domain;

import com.k8s.cnapp.server.auth.domain.Tenant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 모든 자산(Resource Profile)의 공통 필드를 관리하는 부모 클래스
 */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseResourceProfile {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    protected Tenant tenant;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "last_seen_at")
    protected LocalDateTime lastSeenAt;

    protected BaseResourceProfile(Tenant tenant) {
        this.tenant = tenant;
    }

    public void updateLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
