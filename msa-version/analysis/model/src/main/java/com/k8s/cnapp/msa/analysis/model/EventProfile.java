package com.k8s.cnapp.msa.analysis.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
public class EventProfile extends BaseResourceProfile {

    private String namespace;
    private String involvedObjectKind;
    private String involvedObjectName;
    private String reason;
    private String message;
    private String type;
    private Integer count;
    private OffsetDateTime lastTimestamp;
    private String uid;

    protected EventProfile() {
    }

    public EventProfile(Tenant tenant, String namespace, String kind, String name, String reason, String message, String type, Integer count, OffsetDateTime lastTimestamp, String uid) {
        super(tenant);
        this.namespace = namespace;
        this.involvedObjectKind = kind;
        this.involvedObjectName = name;
        this.reason = reason;
        this.message = message;
        this.type = type;
        this.count = count;
        this.lastTimestamp = lastTimestamp;
        this.uid = uid;
    }

    public void update(Integer count, OffsetDateTime lastTimestamp, String message) {
        this.count = count;
        this.lastTimestamp = lastTimestamp;
        this.message = message;
        updateLastSeenAt(LocalDateTime.now());
    }

    /** 영속성 어댑터 전용 복원 팩토리. */
    public static EventProfile restore(Long id, Tenant tenant, String namespace, String involvedObjectKind, String involvedObjectName, String reason, String message, String type, Integer count, OffsetDateTime lastTimestamp, String uid, LocalDateTime createdAt, LocalDateTime lastSeenAt) {
        EventProfile profile = new EventProfile();
        profile.restoreBase(id, tenant, createdAt, lastSeenAt);
        profile.namespace = namespace;
        profile.involvedObjectKind = involvedObjectKind;
        profile.involvedObjectName = involvedObjectName;
        profile.reason = reason;
        profile.message = message;
        profile.type = type;
        profile.count = count;
        profile.lastTimestamp = lastTimestamp;
        profile.uid = uid;
        return profile;
    }
}
