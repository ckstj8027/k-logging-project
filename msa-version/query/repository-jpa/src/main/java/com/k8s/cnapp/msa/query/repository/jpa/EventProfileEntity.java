package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.model.EventProfile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 기존 query 도메인의 EventProfile 엔티티와 동일한 매핑.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_profiles")
public class EventProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_profile_seq")
    @SequenceGenerator(name = "event_profile_seq", sequenceName = "event_profile_seq", allocationSize = 50)
    private Long id;
    private Long tenantId;
    private String namespace;
    private String involvedObjectKind;
    private String involvedObjectName;
    private String reason;
    private String message;
    private String type;
    private Integer count;
    private OffsetDateTime lastTimestamp;
    private String uid;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;

    public EventProfile toModel() {
        return new EventProfile(id, tenantId, namespace, involvedObjectKind, involvedObjectName,
                reason, message, type, count, lastTimestamp, uid, createdAt, lastSeenAt);
    }
}
