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
@Table(name = "deployment_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_deployment_profile", columnNames = {"tenant_id", "namespace", "name"})
})
public class DeploymentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String namespace;

    @Column(nullable = false)
    private String name;

    @Column(name = "replicas")
    private Integer replicas;

    @Column(name = "available_replicas")
    private Integer availableReplicas;

    @Column(name = "strategy_type")
    private String strategyType; // RollingUpdate, Recreate

    @Column(name = "selector_json", columnDefinition = "TEXT")
    private String selectorJson;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public DeploymentProfile(Tenant tenant, String namespace, String name, Integer replicas, Integer availableReplicas, String strategyType, String selectorJson) {
        this.tenant = tenant;
        this.namespace = namespace;
        this.name = name;
        this.replicas = replicas;
        this.availableReplicas = availableReplicas;
        this.strategyType = strategyType;
        this.selectorJson = selectorJson;
    }

    public void update(Integer replicas, Integer availableReplicas, String strategyType, String selectorJson) {
        this.replicas = replicas;
        this.availableReplicas = availableReplicas;
        this.strategyType = strategyType;
        this.selectorJson = selectorJson;
    }
}
