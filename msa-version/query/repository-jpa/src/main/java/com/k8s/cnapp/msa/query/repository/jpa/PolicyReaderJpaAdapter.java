package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.query.model.Policy;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
class PolicyReaderJpaAdapter implements PolicyReader {

    private final PolicyJpaRepository policyJpaRepository;

    @Override
    public List<Policy> findAllByTenantId(Long tenantId) {
        return policyJpaRepository.findAllByTenantId(tenantId).stream()
                .map(PolicyEntity::toModel)
                .toList();
    }

    @Override
    public Optional<Policy> findById(Long id) {
        return policyJpaRepository.findById(id).map(PolicyEntity::toModel);
    }
}
