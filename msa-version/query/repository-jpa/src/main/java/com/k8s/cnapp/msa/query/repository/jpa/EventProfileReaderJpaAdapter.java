package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.EventProfileReader;
import com.k8s.cnapp.msa.query.model.EventProfile;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class EventProfileReaderJpaAdapter implements EventProfileReader {

    private final EventProfileJpaRepository eventProfileJpaRepository;

    @Override
    public List<EventProfile> findAllByTenantId(Long tenantId) {
        return eventProfileJpaRepository.findAllByTenantId(tenantId).stream()
                .map(EventProfileEntity::toModel)
                .toList();
    }
}
