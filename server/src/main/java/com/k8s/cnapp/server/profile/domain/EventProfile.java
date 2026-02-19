package com.k8s.cnapp.server.profile.domain;

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

    // Event는 보통 누적되는 로그 성격이 강하므로 UpdateTimestamp 대신 새로운 레코드로 쌓거나,
    // 동일한 이벤트(UID 기준)가 오면 count만 증가시키는 전략을 사용함.
    // 여기서는 단순화를 위해 매번 새로운 레코드로 저장하되, 너무 많으면 주기적으로 삭제하는 정책 필요.
    // 또는 UID를 Unique Key로 잡고 Count만 업데이트할 수도 있음.

    @Column(name = "uid", unique = true)
    private String uid;

    public EventProfile(String namespace, String involvedObjectKind, String involvedObjectName, String reason, String message, String type, OffsetDateTime lastTimestamp, Integer count, String uid) {
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
