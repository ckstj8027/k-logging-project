package com.k8s.cnapp.msa.auth.repository.jpa;

import com.k8s.cnapp.msa.auth.model.Policy;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policies")
public class PolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    private String value;
    private String description;
    private boolean enabled;

    PolicyEntity(TenantEntity tenant, ResourceType resourceType, RuleType ruleType,
                 String value, String description, boolean enabled) {
        this.tenant = tenant;
        this.resourceType = resourceType;
        this.ruleType = ruleType;
        this.value = value;
        this.description = description;
        this.enabled = enabled;
    }

    Policy toModel() {
        return new Policy(id, tenant.getId(), resourceType, ruleType, value, description, enabled);
    }
}
