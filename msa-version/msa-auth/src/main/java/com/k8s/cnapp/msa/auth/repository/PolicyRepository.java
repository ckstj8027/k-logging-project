package com.k8s.cnapp.msa.auth.repository;

import com.k8s.cnapp.msa.auth.domain.Policy;
import com.k8s.cnapp.msa.auth.domain.Tenant;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByTenantAndResourceTypeAndRuleType(Tenant tenant, ResourceType resourceType, RuleType ruleType);
}
