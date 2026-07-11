package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.common.model.Category;
import com.k8s.cnapp.msa.common.model.Severity;
import com.k8s.cnapp.msa.common.model.Status;
import com.k8s.cnapp.msa.query.model.Alert;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기존 query 도메인의 Alert 엔티티와 동일한 매핑.
 * (주의: query 관점에서는 tenant_id 를 연관관계가 아닌 단순 Long 컬럼으로 매핑한다)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "alerts")
public class AlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alert_seq")
    @SequenceGenerator(name = "alert_seq", sequenceName = "alert_seq", allocationSize = 50)
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

    public Alert toModel() {
        return new Alert(id, tenantId, severity, category, message, resourceType, resourceName, status, createdAt);
    }
}
