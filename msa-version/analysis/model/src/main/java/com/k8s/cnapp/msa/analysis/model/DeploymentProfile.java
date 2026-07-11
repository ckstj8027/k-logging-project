package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DeploymentProfile extends BaseResourceProfile {

    private String namespace;
    private String name;
    private Integer replicas;
    private Integer availableReplicas;
    private String strategyType;
    private String selectorJson;

    protected DeploymentProfile() {
    }

    public DeploymentProfile(Tenant tenant, String namespace, String name, Integer replicas, Integer availableReplicas, String strategyType, String selectorJson) {
        super(tenant);
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
        updateLastSeenAt(LocalDateTime.now());
    }

    /** 영속성 어댑터 전용 복원 팩토리. */
    public static DeploymentProfile restore(Long id, Tenant tenant, String namespace, String name, Integer replicas, Integer availableReplicas, String strategyType, String selectorJson, LocalDateTime createdAt, LocalDateTime lastSeenAt) {
        DeploymentProfile profile = new DeploymentProfile();
        profile.restoreBase(id, tenant, createdAt, lastSeenAt);
        profile.namespace = namespace;
        profile.name = name;
        profile.replicas = replicas;
        profile.availableReplicas = availableReplicas;
        profile.strategyType = strategyType;
        profile.selectorJson = selectorJson;
        return profile;
    }
}
