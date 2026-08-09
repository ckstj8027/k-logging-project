package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.EventProfileReader;
import com.k8s.cnapp.msa.query.model.EventProfile;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Override
    public List<EventProfile> findPage(Long tenantId, Long lastId, int size) {
        Pageable page = PageRequest.of(0, size);
        List<EventProfileEntity> entities = (lastId == null)
                ? eventProfileJpaRepository.findByTenantIdOrderByIdDesc(tenantId, page)
                : eventProfileJpaRepository.findByTenantIdAndIdLessThanOrderByIdDesc(tenantId, lastId, page);
        return entities.stream()
                .map(EventProfileEntity::toModel)
                .toList();
    }
}
