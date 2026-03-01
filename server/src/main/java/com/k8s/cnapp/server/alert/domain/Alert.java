package com.k8s.cnapp.server.alert.domain;

import com.k8s.cnapp.server.auth.domain.Tenant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private String message;

    @Column(name = "resource_type")
    private String resourceType; // Pod, Service, etc.

    @Column(name = "resource_name")
    private String resourceName; // namespace/name

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Alert(Tenant tenant, Severity severity, Category category, String message, String resourceType, String resourceName) {
        this.tenant = tenant;
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.status = Status.OPEN;
    }

    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public enum Category {
        CSPM, // 설정 보안 (Drift Detection)
        RUNTIME // 런타임 보안 (eBPF 등)
    }

    public enum Status {
        OPEN, RESOLVED
    }
}
