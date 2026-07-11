package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.model.NamespaceProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "namespace_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_namespace_profile", columnNames = {"tenant_id", "name"})
})
public class NamespaceProfileEntity extends BaseResourceProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "namespace_profile_seq")
    @SequenceGenerator(name = "namespace_profile_seq", sequenceName = "namespace_profile_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String status;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    static NamespaceProfileEntity fromModel(NamespaceProfile model, TenantEntity tenant) {
        NamespaceProfileEntity entity = new NamespaceProfileEntity();
        entity.tenant = tenant;
        entity.name = model.getName();
        entity.apply(model);
        return entity;
    }

    void apply(NamespaceProfile model) {
        this.status = model.getStatus();
        this.lastSeenAt = model.getLastSeenAt();
    }

    NamespaceProfile toModel(Tenant tenant) {
        return NamespaceProfile.restore(id, tenant, name, status, createdAt, lastSeenAt);
    }
}
