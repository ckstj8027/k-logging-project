package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.asset.NodeProfileStore;
import com.k8s.cnapp.msa.analysis.model.NodeProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class NodeProfileStoreJpaAdapter implements NodeProfileStore {

    private final NodeProfileJpaRepository nodeProfileJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<NodeProfile> findByTenantAndName(Tenant tenant, String name) {
        return nodeProfileJpaRepository
                .findByTenantAndName(tenantJpaRepository.getReferenceById(tenant.getId()), name)
                .map(entity -> entity.toModel(tenant));
    }

    @Override
    public NodeProfile save(NodeProfile profile) {
        NodeProfileEntity entity = (profile.getId() == null)
                ? NodeProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId()))
                : nodeProfileJpaRepository.findById(profile.getId())
                        .map(existing -> {
                            existing.apply(profile);
                            return existing;
                        })
                        .orElseGet(() -> NodeProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId())));
        return nodeProfileJpaRepository.save(entity).toModel(profile.getTenant());
    }
}
