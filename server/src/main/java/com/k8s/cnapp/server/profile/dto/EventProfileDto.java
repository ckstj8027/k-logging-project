package com.k8s.cnapp.server.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.k8s.cnapp.server.profile.domain.EventProfile;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
public class EventProfileDto {
    private Long id;
    private String namespace;
    private String involvedObjectKind;
    private String involvedObjectName;
    private String reason;
    private String message;
    private String type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime lastTimestamp;

    private Integer count;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String uid;

    public EventProfileDto(EventProfile eventProfile) {
        this.id = eventProfile.getId();
        this.namespace = eventProfile.getNamespace();
        this.involvedObjectKind = eventProfile.getInvolvedObjectKind();
        this.involvedObjectName = eventProfile.getInvolvedObjectName();
        this.reason = eventProfile.getReason();
        this.message = eventProfile.getMessage();
        this.type = eventProfile.getType();
        this.lastTimestamp = eventProfile.getLastTimestamp();
        this.count = eventProfile.getCount();
        this.createdAt = eventProfile.getCreatedAt();
        this.uid = eventProfile.getUid();
    }
}
