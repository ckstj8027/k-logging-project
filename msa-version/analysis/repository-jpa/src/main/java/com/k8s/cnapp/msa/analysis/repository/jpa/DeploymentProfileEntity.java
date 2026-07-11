package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.model.DeploymentProfile;
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
@Table(name = "deployment_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_deployment_profile", columnNames = {"tenant_id", "namespace", "name"})
})
public class DeploymentProfileEntity extends BaseResourceProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deployment_profile_seq")
    @SequenceGenerator(name = "deployment_profile_seq", sequenceName = "deployment_profile_seq", allocationSize = 50)
    private Long id;

    private String namespace;
    private String name;
    private Integer replicas;
    private Integer availableReplicas;
    private String strategyType;

    @Column(columnDefinition = "TEXT")
    private String selectorJson;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    static DeploymentProfileEntity fromModel(DeploymentProfile model, TenantEntity tenant) {
        DeploymentProfileEntity entity = new DeploymentProfileEntity();
        entity.tenant = tenant;
        entity.namespace = model.getNamespace();
        entity.name = model.getName();
        entity.apply(model);
        return entity;
    }

    void apply(DeploymentProfile model) {
        this.replicas = model.getReplicas();
        this.availableReplicas = model.getAvailableReplicas();
        this.strategyType = model.getStrategyType();
        this.selectorJson = model.getSelectorJson();
        this.lastSeenAt = model.getLastSeenAt();
    }

    DeploymentProfile toModel(Tenant tenant) {
        return DeploymentProfile.restore(id, tenant, namespace, name, replicas, availableReplicas, strategyType, selectorJson, createdAt, lastSeenAt);
    }
}
