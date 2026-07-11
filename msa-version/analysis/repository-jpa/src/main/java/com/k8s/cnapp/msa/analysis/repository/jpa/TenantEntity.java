package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.model.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tenants")
public class TenantEntity {

    @Id
    private Long id; // MSA 환경에서는 ID를 직접 할당받아 동기화합니다.

    @Column(nullable = false)
    private String name;

    private LocalDateTime createdAt;

    static TenantEntity fromModel(Tenant tenant) {
        TenantEntity entity = new TenantEntity();
        entity.id = tenant.getId();
        entity.name = tenant.getName();
        entity.createdAt = tenant.getCreatedAt();
        return entity;
    }

    Tenant toModel() {
        return new Tenant(id, name, createdAt);
    }
}
