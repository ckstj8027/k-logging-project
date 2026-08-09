package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.DeploymentProfileReader;
import com.k8s.cnapp.msa.query.model.DeploymentProfile;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
class DeploymentProfileReaderJpaAdapter implements DeploymentProfileReader {

    private final DeploymentProfileJpaRepository deploymentProfileJpaRepository;

    @Override
    public List<DeploymentProfile> findAllByTenantId(Long tenantId) {
        return deploymentProfileJpaRepository.findAllByTenantId(tenantId).stream()
                .map(DeploymentProfileEntity::toModel)
                .toList();
    }

    @Override
    public List<DeploymentProfile> findPage(Long tenantId, Long lastId, int size) {
        Pageable page = PageRequest.of(0, size);
        List<DeploymentProfileEntity> entities = (lastId == null)
                ? deploymentProfileJpaRepository.findByTenantIdOrderByIdDesc(tenantId, page)
                : deploymentProfileJpaRepository.findByTenantIdAndIdLessThanOrderByIdDesc(tenantId, lastId, page);
        return entities.stream()
                .map(DeploymentProfileEntity::toModel)
                .toList();
    }
}
