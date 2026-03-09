package com.k8s.cnapp.server.alert.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.k8s.cnapp.server.alert.domain.Alert;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AlertDto {
    private Long id;
    private String severity;
    private String category;
    private String message;
    private String resourceType;
    private String resourceName;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public AlertDto(Alert alert) {
        this.id = alert.getId();
        this.severity = alert.getSeverity().name();
        this.category = alert.getCategory().name();
        this.message = alert.getMessage();
        this.resourceType = alert.getResourceType();
        this.resourceName = alert.getResourceName();
        this.status = alert.getStatus().name();
        this.createdAt = alert.getCreatedAt();
    }
}
