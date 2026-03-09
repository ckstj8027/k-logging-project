package com.k8s.cnapp.server.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.k8s.cnapp.server.profile.domain.PodProfile;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PodProfileDto {
    private Long id;
    private String namespace;
    private String podName;
    private String containerName;
    private String image;
    private String deploymentName;
    private Boolean privileged;
    private Long runAsUser;
    private Boolean runAsRoot;
    private Boolean allowPrivilegeEscalation;
    private Boolean readOnlyRootFilesystem;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public PodProfileDto(PodProfile podProfile) {
        this.id = podProfile.getId();
        this.namespace = podProfile.getAssetContext().getNamespace();
        this.podName = podProfile.getAssetContext().getPodName();
        this.containerName = podProfile.getAssetContext().getContainerName();
        this.image = podProfile.getAssetContext().getImage();
        this.deploymentName = podProfile.getAssetContext().getDeploymentName();
        this.privileged = podProfile.getPrivileged();
        this.runAsUser = podProfile.getRunAsUser();
        this.runAsRoot = podProfile.getRunAsRoot();
        this.allowPrivilegeEscalation = podProfile.getAllowPrivilegeEscalation();
        this.readOnlyRootFilesystem = podProfile.getReadOnlyRootFilesystem();
        this.createdAt = podProfile.getCreatedAt();
        this.updatedAt = podProfile.getUpdatedAt();
    }
}
