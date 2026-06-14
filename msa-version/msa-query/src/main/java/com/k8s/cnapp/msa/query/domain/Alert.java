package com.k8s.cnapp.msa.query.domain;

import com.k8s.cnapp.msa.common.model.Category;
import com.k8s.cnapp.msa.common.model.Severity;
import com.k8s.cnapp.msa.common.model.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "alerts")
public class Alert {
    @Id
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String message;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_name")
    private String resourceName;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
