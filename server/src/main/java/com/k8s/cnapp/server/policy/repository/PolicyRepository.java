package com.k8s.cnapp.server.policy.repository;

import com.k8s.cnapp.server.policy.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByResourceTypeAndRuleType(Policy.ResourceType resourceType, Policy.RuleType ruleType);
}
