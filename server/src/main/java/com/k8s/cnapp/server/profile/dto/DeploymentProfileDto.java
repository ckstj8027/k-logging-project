package com.k8s.cnapp.server.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DeploymentProfileDto {
    private Long id;
    private String namespace;
    private String name;
    private Integer replicas;
    private Integer availableReplicas;
    private String strategyType;
    private String selectorJson;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public DeploymentProfileDto(DeploymentProfile deploymentProfile) {
        this.id = deploymentProfile.getId();
        this.namespace = deploymentProfile.getNamespace();
        this.name = deploymentProfile.getName();
        this.replicas = deploymentProfile.getReplicas();
        this.availableReplicas = deploymentProfile.getAvailableReplicas();
        this.strategyType = deploymentProfile.getStrategyType();
        this.selectorJson = deploymentProfile.getSelectorJson();
        this.createdAt = deploymentProfile.getCreatedAt();
        this.updatedAt = deploymentProfile.getUpdatedAt();
    }
}
