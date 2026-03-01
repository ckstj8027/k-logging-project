package com.k8s.cnapp.server.policy.dto;

import com.k8s.cnapp.server.policy.domain.Policy;
import lombok.Getter;

@Getter
public class PolicyDto {
    private Long id;
    private String resourceType;
    private String ruleType;
    private String value;
    private boolean enabled;
    private String description;

    public PolicyDto(Policy policy) {
        this.id = policy.getId();
        this.resourceType = policy.getResourceType().name();
        this.ruleType = policy.getRuleType().name();
        this.value = policy.getValue();
        this.enabled = policy.isEnabled();
        this.description = policy.getDescription();
    }
}
