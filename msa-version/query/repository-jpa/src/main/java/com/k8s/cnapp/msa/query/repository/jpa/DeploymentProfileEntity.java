package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.model.DeploymentProfile;
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
 * 기존 query 도메인의 DeploymentProfile 엔티티와 동일한 매핑.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "deployment_profiles")
public class DeploymentProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deployment_profile_seq")
    @SequenceGenerator(name = "deployment_profile_seq", sequenceName = "deployment_profile_seq", allocationSize = 50)
    private Long id;
    private Long tenantId;
    private String namespace;
    private String name;
    private Integer replicas;
    private Integer availableReplicas;
    private String strategyType;
    private String selectorJson;
    private LocalDateTime lastSeenAt;

    public DeploymentProfile toModel() {
        return new DeploymentProfile(id, tenantId, namespace, name, replicas, availableReplicas,
                strategyType, selectorJson, lastSeenAt);
    }
}
