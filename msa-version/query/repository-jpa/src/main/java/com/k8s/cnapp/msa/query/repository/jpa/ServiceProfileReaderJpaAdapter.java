package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.ServiceProfileReader;
import com.k8s.cnapp.msa.query.model.ServiceProfile;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class ServiceProfileReaderJpaAdapter implements ServiceProfileReader {

    private final ServiceProfileJpaRepository serviceProfileJpaRepository;

    @Override
    public List<ServiceProfile> findAllByTenantId(Long tenantId) {
        return serviceProfileJpaRepository.findAllByTenantId(tenantId).stream()
                .map(ServiceProfileEntity::toModel)
                .toList();
    }
}
