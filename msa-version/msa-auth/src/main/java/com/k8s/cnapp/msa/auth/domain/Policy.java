package com.k8s.cnapp.msa.auth.domain;

import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    private String value;
    private String description;
    private boolean enabled;

    public Policy(Tenant tenant, ResourceType resourceType, RuleType ruleType, String value, String description) {
        this.tenant = tenant;
        this.resourceType = resourceType;
        this.ruleType = ruleType;
        this.value = value;
        this.description = description;
        this.enabled = true;
    }
}
