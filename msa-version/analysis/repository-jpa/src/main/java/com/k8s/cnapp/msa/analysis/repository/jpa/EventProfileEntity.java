package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.model.EventProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_profiles")
public class EventProfileEntity extends BaseResourceProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_profile_seq")
    @SequenceGenerator(name = "event_profile_seq", sequenceName = "event_profile_seq", allocationSize = 50)
    private Long id;

    private String namespace;
    private String involvedObjectKind;
    private String involvedObjectName;
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String type;
    private Integer count;
    private OffsetDateTime lastTimestamp;

    @Column(unique = true)
    private String uid;

    static EventProfileEntity fromModel(EventProfile model, TenantEntity tenant) {
        EventProfileEntity entity = new EventProfileEntity();
        entity.tenant = tenant;
        entity.namespace = model.getNamespace();
        entity.involvedObjectKind = model.getInvolvedObjectKind();
        entity.involvedObjectName = model.getInvolvedObjectName();
        entity.reason = model.getReason();
        entity.type = model.getType();
        entity.uid = model.getUid();
        entity.apply(model);
        return entity;
    }

    void apply(EventProfile model) {
        this.count = model.getCount();
        this.lastTimestamp = model.getLastTimestamp();
        this.message = model.getMessage();
        this.lastSeenAt = model.getLastSeenAt();
    }

    EventProfile toModel(Tenant tenant) {
        return EventProfile.restore(id, tenant, namespace, involvedObjectKind, involvedObjectName, reason, message, type, count, lastTimestamp, uid, createdAt, lastSeenAt);
    }
}
