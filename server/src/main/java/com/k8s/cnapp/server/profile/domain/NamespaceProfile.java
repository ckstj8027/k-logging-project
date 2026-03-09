package com.k8s.cnapp.server.profile.domain;

import com.k8s.cnapp.server.auth.domain.Tenant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "namespace_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_namespace_profile", columnNames = {"tenant_id", "name"})
}, indexes = {
        @Index(name = "idx_namespace_last_seen", columnList = "last_seen_at")
})
public class NamespaceProfile extends BaseResourceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "namespace_profile_seq")
    @SequenceGenerator(name = "namespace_profile_seq", sequenceName = "namespace_profile_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "status")
    private String status; // Active, Terminating

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public NamespaceProfile(Tenant tenant, String name, String status) {
        super(tenant);
        this.name = name;
        this.status = status;
    }

    public void update(String status) {
        this.status = status;
    }
}
