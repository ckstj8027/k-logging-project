package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import com.k8s.cnapp.msa.query.model.Policy;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기존 query 도메인의 Policy 엔티티와 동일한 매핑.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policies")
public class PolicyEntity {
    @Id private Long id;
    private Long tenantId;
    @Enumerated(EnumType.STRING) private ResourceType resourceType;
    @Enumerated(EnumType.STRING) private RuleType ruleType;
    private String value;
    private boolean enabled;

    public Policy toModel() {
        return new Policy(id, tenantId, resourceType, ruleType, value, enabled);
    }

    static PolicyEntity fromModel(Policy model) {
        PolicyEntity entity = new PolicyEntity();
        entity.id = model.getId();
        entity.tenantId = model.getTenantId();
        entity.resourceType = model.getResourceType();
        entity.ruleType = model.getRuleType();
        entity.value = model.getValue();
        entity.enabled = model.isEnabled();
        return entity;
    }
}
