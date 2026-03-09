package com.k8s.cnapp.server.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ServiceProfileDto {
    private Long id;
    private String namespace;
    private String name;
    private String type;
    private String clusterIp;
    private String externalIps;
    private List<ServicePortProfileDto> ports;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public ServiceProfileDto(ServiceProfile serviceProfile) {
        this.id = serviceProfile.getId();
        this.namespace = serviceProfile.getNamespace();
        this.name = serviceProfile.getName();
        this.type = serviceProfile.getType();
        this.clusterIp = serviceProfile.getClusterIp();
        this.externalIps = serviceProfile.getExternalIps();
        this.ports = serviceProfile.getPorts().stream()
                .map(ServicePortProfileDto::new)
                .collect(Collectors.toList());
        this.createdAt = serviceProfile.getCreatedAt();
        this.updatedAt = serviceProfile.getUpdatedAt();
    }
}
