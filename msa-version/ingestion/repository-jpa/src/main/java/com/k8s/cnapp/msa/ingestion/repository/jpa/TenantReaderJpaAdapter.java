package com.k8s.cnapp.msa.ingestion.repository.jpa;

import com.k8s.cnapp.msa.ingestion.infrastructure.tenant.TenantReader;
import com.k8s.cnapp.msa.ingestion.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class TenantReaderJpaAdapter implements TenantReader {

    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<Tenant> findByApiKey(String apiKey) {
        return tenantJpaRepository.findByApiKey(apiKey).map(TenantEntity::toModel);
    }
}
