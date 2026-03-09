package com.k8s.cnapp.server.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.k8s.cnapp.server.profile.domain.NodeProfile;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NodeProfileDto {
    private Long id;
    private String name;
    private String osImage;
    private String kernelVersion;
    private String containerRuntimeVersion;
    private String kubeletVersion;
    private String cpuCapacity;
    private String memoryCapacity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public NodeProfileDto(NodeProfile nodeProfile) {
        this.id = nodeProfile.getId();
        this.name = nodeProfile.getName();
        this.osImage = nodeProfile.getOsImage();
        this.kernelVersion = nodeProfile.getKernelVersion();
        this.containerRuntimeVersion = nodeProfile.getContainerRuntimeVersion();
        this.kubeletVersion = nodeProfile.getKubeletVersion();
        this.cpuCapacity = nodeProfile.getCpuCapacity();
        this.memoryCapacity = nodeProfile.getMemoryCapacity();
        this.createdAt = nodeProfile.getCreatedAt();
        this.updatedAt = nodeProfile.getUpdatedAt();
    }
}
