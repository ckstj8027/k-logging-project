package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.tenant.TenantStore;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class TenantStoreJpaAdapter implements TenantStore {

    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<Tenant> findById(Long id) {
        return tenantJpaRepository.findById(id).map(TenantEntity::toModel);
    }

    @Override
    public Tenant save(Tenant tenant) {
        return tenantJpaRepository.save(TenantEntity.fromModel(tenant)).toModel();
    }
}
