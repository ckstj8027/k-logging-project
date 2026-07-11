package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.asset.DeploymentProfileReader;
import com.k8s.cnapp.msa.query.model.DeploymentProfile;
import lombok.RequiredArgsConstructor;

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
}
