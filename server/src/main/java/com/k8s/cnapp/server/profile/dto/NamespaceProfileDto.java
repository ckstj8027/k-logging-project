package com.k8s.cnapp.server.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.k8s.cnapp.server.profile.domain.NamespaceProfile;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NamespaceProfileDto {
    private Long id;
    private String name;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public NamespaceProfileDto(NamespaceProfile namespaceProfile) {
        this.id = namespaceProfile.getId();
        this.name = namespaceProfile.getName();
        this.status = namespaceProfile.getStatus();
        this.createdAt = namespaceProfile.getCreatedAt();
        this.updatedAt = namespaceProfile.getUpdatedAt();
    }
}
