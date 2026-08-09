package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.NodeProfileReader;
import com.k8s.cnapp.msa.query.model.NodeProfile;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Override
    public List<NodeProfile> findPage(Long tenantId, Long lastId, int size) {
        Pageable page = PageRequest.of(0, size);
        List<NodeProfileEntity> entities = (lastId == null)
                ? nodeProfileJpaRepository.findByTenantIdOrderByIdDesc(tenantId, page)
                : nodeProfileJpaRepository.findByTenantIdAndIdLessThanOrderByIdDesc(tenantId, lastId, page);
        return entities.stream()
                .map(NodeProfileEntity::toModel)
                .toList();
    }
}
