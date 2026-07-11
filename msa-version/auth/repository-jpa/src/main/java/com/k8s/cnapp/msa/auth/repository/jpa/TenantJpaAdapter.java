package com.k8s.cnapp.msa.auth.repository.jpa;

import com.k8s.cnapp.msa.auth.infrastructure.tenant.TenantReader;
import com.k8s.cnapp.msa.auth.infrastructure.tenant.TenantWriter;
import com.k8s.cnapp.msa.auth.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class TenantJpaAdapter implements TenantReader, TenantWriter {

    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<Tenant> findByName(String name) {
        return tenantJpaRepository.findByName(name).map(TenantEntity::toModel);
    }

    @Override
    public Tenant save(Tenant tenant) {
        return tenantJpaRepository.save(TenantEntity.from(tenant)).toModel();
    }
}
