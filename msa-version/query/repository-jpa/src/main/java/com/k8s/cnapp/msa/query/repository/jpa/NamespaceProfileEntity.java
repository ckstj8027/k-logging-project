package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.model.NamespaceProfile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기존 query 도메인의 NamespaceProfile 엔티티와 동일한 매핑.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "namespace_profiles")
public class NamespaceProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "namespace_profile_seq")
    @SequenceGenerator(name = "namespace_profile_seq", sequenceName = "namespace_profile_seq", allocationSize = 50)
    private Long id;
    private Long tenantId;
    private String name;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;

    public NamespaceProfile toModel() {
        return new NamespaceProfile(id, tenantId, name, status, createdAt, lastSeenAt);
    }
}
