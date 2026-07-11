package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.asset.DeploymentProfileStore;
import com.k8s.cnapp.msa.analysis.model.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class DeploymentProfileStoreJpaAdapter implements DeploymentProfileStore {

    private final DeploymentProfileJpaRepository deploymentProfileJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<DeploymentProfile> findByTenantAndNamespaceAndName(Tenant tenant, String namespace, String name) {
        return deploymentProfileJpaRepository
                .findByTenantAndNamespaceAndName(tenantJpaRepository.getReferenceById(tenant.getId()), namespace, name)
                .map(entity -> entity.toModel(tenant));
    }

    @Override
    public DeploymentProfile save(DeploymentProfile profile) {
        DeploymentProfileEntity entity = (profile.getId() == null)
                ? DeploymentProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId()))
                : deploymentProfileJpaRepository.findById(profile.getId())
                        .map(existing -> {
                            existing.apply(profile);
                            return existing;
                        })
                        .orElseGet(() -> DeploymentProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId())));
        return deploymentProfileJpaRepository.save(entity).toModel(profile.getTenant());
    }
}
