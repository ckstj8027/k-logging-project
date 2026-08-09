package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.ServiceProfileReader;
import com.k8s.cnapp.msa.query.model.ServiceProfile;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Override
    public List<ServiceProfile> findPage(Long tenantId, Long lastId, int size) {
        Pageable page = PageRequest.of(0, size);
        List<ServiceProfileEntity> entities = (lastId == null)
                ? serviceProfileJpaRepository.findByTenantIdOrderByIdDesc(tenantId, page)
                : serviceProfileJpaRepository.findByTenantIdAndIdLessThanOrderByIdDesc(tenantId, lastId, page);
        return entities.stream()
                .map(ServiceProfileEntity::toModel)
                .toList();
    }
}
