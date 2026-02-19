package com.k8s.cnapp.server.profile.domain;

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
        @UniqueConstraint(name = "uk_namespace_profile", columnNames = {"name"})
})
public class NamespaceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "status")
    private String status; // Active, Terminating

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public NamespaceProfile(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public void update(String status) {
        this.status = status;
    }
}
