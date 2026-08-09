package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.NamespaceProfileReader;
import com.k8s.cnapp.msa.query.model.NamespaceProfile;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
class NamespaceProfileReaderJpaAdapter implements NamespaceProfileReader {

    private final NamespaceProfileJpaRepository namespaceProfileJpaRepository;

    @Override
    public List<NamespaceProfile> findAllByTenantId(Long tenantId) {
        return namespaceProfileJpaRepository.findAllByTenantId(tenantId).stream()
                .map(NamespaceProfileEntity::toModel)
                .toList();
    }

    @Override
    public List<NamespaceProfile> findPage(Long tenantId, Long lastId, int size) {
        Pageable page = PageRequest.of(0, size);
        List<NamespaceProfileEntity> entities = (lastId == null)
                ? namespaceProfileJpaRepository.findByTenantIdOrderByIdDesc(tenantId, page)
                : namespaceProfileJpaRepository.findByTenantIdAndIdLessThanOrderByIdDesc(tenantId, lastId, page);
        return entities.stream()
                .map(NamespaceProfileEntity::toModel)
                .toList();
    }
}
