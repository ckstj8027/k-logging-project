package com.k8s.cnapp.msa.auth.infrastructure.policy;

import com.k8s.cnapp.msa.auth.model.Policy;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;

import java.util.Optional;

/**
 * Out-Port: 정책 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface PolicyReader {
    Optional<Policy> findByTenantIdAndResourceTypeAndRuleType(Long tenantId, ResourceType resourceType, RuleType ruleType);
}
