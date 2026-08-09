package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.PodProfileReader;
import com.k8s.cnapp.msa.query.model.PodProfile;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Override
    public List<PodProfile> findPage(Long tenantId, Long lastId, int size) {
        Pageable page = PageRequest.of(0, size);
        List<PodProfileEntity> entities = (lastId == null)
                ? podProfileJpaRepository.findByTenantIdOrderByIdDesc(tenantId, page)
                : podProfileJpaRepository.findByTenantIdAndIdLessThanOrderByIdDesc(tenantId, lastId, page);
        return entities.stream()
                .map(PodProfileEntity::toModel)
                .toList();
    }
}
