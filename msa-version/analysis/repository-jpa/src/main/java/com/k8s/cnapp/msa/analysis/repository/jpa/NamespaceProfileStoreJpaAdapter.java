package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.asset.NamespaceProfileStore;
import com.k8s.cnapp.msa.analysis.model.NamespaceProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class NamespaceProfileStoreJpaAdapter implements NamespaceProfileStore {

    private final NamespaceProfileJpaRepository namespaceProfileJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<NamespaceProfile> findByTenantAndName(Tenant tenant, String name) {
        return namespaceProfileJpaRepository
                .findByTenantAndName(tenantJpaRepository.getReferenceById(tenant.getId()), name)
                .map(entity -> entity.toModel(tenant));
    }

    @Override
    public NamespaceProfile save(NamespaceProfile profile) {
        NamespaceProfileEntity entity = (profile.getId() == null)
                ? NamespaceProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId()))
                : namespaceProfileJpaRepository.findById(profile.getId())
                        .map(existing -> {
                            existing.apply(profile);
                            return existing;
                        })
                        .orElseGet(() -> NamespaceProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId())));
        return namespaceProfileJpaRepository.save(entity).toModel(profile.getTenant());
    }
}
