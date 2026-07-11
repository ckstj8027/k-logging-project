package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.asset.PodProfileStore;
import com.k8s.cnapp.msa.analysis.model.PodProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class PodProfileStoreJpaAdapter implements PodProfileStore {

    private final PodProfileJpaRepository podProfileJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<PodProfile> findByTenantAndAsset(Tenant tenant, String namespace, String podName, String containerName) {
        return podProfileJpaRepository
                .findByTenantAndAssetContextNamespaceAndAssetContextPodNameAndAssetContextContainerName(
                        tenantJpaRepository.getReferenceById(tenant.getId()), namespace, podName, containerName)
                .map(entity -> entity.toModel(tenant));
    }

    @Override
    public PodProfile save(PodProfile profile) {
        PodProfileEntity entity = (profile.getId() == null)
                ? PodProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId()))
                : podProfileJpaRepository.findById(profile.getId())
                        .map(existing -> {
                            existing.apply(profile);
                            return existing;
                        })
                        .orElseGet(() -> PodProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId())));
        return podProfileJpaRepository.save(entity).toModel(profile.getTenant());
    }
}
