package com.k8s.cnapp.server.policy.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.policy.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByTenantAndResourceTypeAndRuleType(Tenant tenant, Policy.ResourceType resourceType, Policy.RuleType ruleType);
    List<Policy> findAllByTenant(Tenant tenant);
}
