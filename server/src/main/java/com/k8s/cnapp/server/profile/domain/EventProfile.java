package com.k8s.cnapp.server.profile.domain;

import com.k8s.cnapp.server.auth.domain.Tenant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_profiles", indexes = {
        @Index(name = "idx_event_timestamp", columnList = "last_timestamp"),
        @Index(name = "idx_event_type", columnList = "type")
})
public class EventProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String namespace;

    @Column(name = "involved_object_kind")
    private String involvedObjectKind;

    @Column(name = "involved_object_name")
    private String involvedObjectName;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String type; // Normal, Warning

    @Column(name = "last_timestamp")
    private OffsetDateTime lastTimestamp;

    @Column(name = "count")
    private Integer count;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "uid", unique = true)
    private String uid;

    public EventProfile(Tenant tenant, String namespace, String involvedObjectKind, String involvedObjectName, String reason, String message, String type, OffsetDateTime lastTimestamp, Integer count, String uid) {
        this.tenant = tenant;
        this.namespace = namespace;
        this.involvedObjectKind = involvedObjectKind;
        this.involvedObjectName = involvedObjectName;
        this.reason = reason;
        this.message = message;
        this.type = type;
        this.lastTimestamp = lastTimestamp;
        this.count = count;
        this.uid = uid;
    }
    
    public void update(Integer count, OffsetDateTime lastTimestamp, String message) {
        this.count = count;
        this.lastTimestamp = lastTimestamp;
        this.message = message;
    }
}
