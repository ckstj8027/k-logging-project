package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.analysis.model.Policy;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class PolicyReaderJpaAdapter implements PolicyReader {

    private final PolicyJpaRepository policyJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public List<Policy> findAllByTenant(Tenant tenant) {
        return policyJpaRepository.findAllByTenant(tenantJpaRepository.getReferenceById(tenant.getId()))
                .stream()
                .map(PolicyEntity::toModel)
                .collect(Collectors.toList());
    }
}
