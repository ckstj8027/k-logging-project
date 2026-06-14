package com.k8s.cnapp.msa.analysis.domain;

import com.k8s.cnapp.msa.common.model.Category;
import com.k8s.cnapp.msa.common.model.Severity;
import com.k8s.cnapp.msa.common.model.Status;
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
    private String resourceType;

    @Column(name = "resource_name")
    private String resourceName;

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
}
