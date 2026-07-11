package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.NodeProfileReader;
import com.k8s.cnapp.msa.query.model.NodeProfile;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class NodeProfileReaderJpaAdapter implements NodeProfileReader {

    private final NodeProfileJpaRepository nodeProfileJpaRepository;

    @Override
    public List<NodeProfile> findAllByTenantId(Long tenantId) {
        return nodeProfileJpaRepository.findAllByTenantId(tenantId).stream()
                .map(NodeProfileEntity::toModel)
                .toList();
    }
}
