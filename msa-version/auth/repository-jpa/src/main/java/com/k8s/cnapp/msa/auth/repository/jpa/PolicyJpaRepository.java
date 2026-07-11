package com.k8s.cnapp.msa.auth.repository.jpa;

import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PolicyJpaRepository extends JpaRepository<PolicyEntity, Long> {
    Optional<PolicyEntity> findByTenantIdAndResourceTypeAndRuleType(Long tenantId, ResourceType resourceType, RuleType ruleType);
}
