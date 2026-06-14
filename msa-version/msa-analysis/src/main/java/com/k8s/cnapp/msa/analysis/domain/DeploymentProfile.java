package com.k8s.cnapp.msa.analysis.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "deployment_profiles")
public class DeploymentProfile extends BaseResourceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String namespace;

    @Column(nullable = false)
    private String name;

    @Column(name = "replicas")
    private Integer replicas;

    @Column(name = "available_replicas")
    private Integer availableReplicas;

    @Column(name = "strategy_type")
    private String strategyType;

    @Column(name = "selector_json", columnDefinition = "TEXT")
    private String selectorJson;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public DeploymentProfile(Tenant tenant, String namespace, String name, Integer replicas, Integer availableReplicas, String strategyType, String selectorJson) {
        super(tenant);
        this.namespace = namespace;
        this.name = name;
        this.replicas = replicas;
        this.availableReplicas = availableReplicas;
        this.strategyType = strategyType;
        this.selectorJson = selectorJson;
    }
}
