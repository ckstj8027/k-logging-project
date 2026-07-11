package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.PodProfileReader;
import com.k8s.cnapp.msa.query.model.PodProfile;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class PodProfileReaderJpaAdapter implements PodProfileReader {

    private final PodProfileJpaRepository podProfileJpaRepository;

    @Override
    public List<PodProfile> findAllByTenantId(Long tenantId) {
        return podProfileJpaRepository.findAllByTenantId(tenantId).stream()
                .map(PodProfileEntity::toModel)
                .toList();
    }
}
