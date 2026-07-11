package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.NamespaceProfileReader;
import com.k8s.cnapp.msa.query.model.NamespaceProfile;
import lombok.RequiredArgsConstructor;

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
}
