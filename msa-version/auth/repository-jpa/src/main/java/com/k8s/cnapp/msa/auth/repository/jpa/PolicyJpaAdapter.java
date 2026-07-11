package com.k8s.cnapp.msa.auth.repository.jpa;

import com.k8s.cnapp.msa.auth.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.auth.infrastructure.policy.PolicyWriter;
import com.k8s.cnapp.msa.auth.model.Policy;
import com.k8s.cnapp.msa.common.model.ResourceType;
import com.k8s.cnapp.msa.common.model.RuleType;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class PolicyJpaAdapter implements PolicyReader, PolicyWriter {

    private final PolicyJpaRepository policyJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<Policy> findByTenantIdAndResourceTypeAndRuleType(Long tenantId, ResourceType resourceType, RuleType ruleType) {
        return policyJpaRepository.findByTenantIdAndResourceTypeAndRuleType(tenantId, resourceType, ruleType)
                .map(PolicyEntity::toModel);
    }

    @Override
    public void save(Policy policy) {
        TenantEntity tenant = tenantJpaRepository.getReferenceById(policy.tenantId());
        policyJpaRepository.save(new PolicyEntity(
                tenant, policy.resourceType(), policy.ruleType(), policy.value(), policy.description(), policy.enabled()));
    }
}
