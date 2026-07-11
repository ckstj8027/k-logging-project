package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.asset.ServiceProfileStore;
import com.k8s.cnapp.msa.analysis.model.ServiceProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class ServiceProfileStoreJpaAdapter implements ServiceProfileStore {

    private final ServiceProfileJpaRepository serviceProfileJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<ServiceProfile> findByTenantAndNamespaceAndName(Tenant tenant, String namespace, String name) {
        return serviceProfileJpaRepository
                .findByTenantAndNamespaceAndName(tenantJpaRepository.getReferenceById(tenant.getId()), namespace, name)
                .map(entity -> entity.toModel(tenant));
    }

    @Override
    public ServiceProfile save(ServiceProfile profile) {
        ServiceProfileEntity entity = (profile.getId() == null)
                ? ServiceProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId()))
                : serviceProfileJpaRepository.findById(profile.getId())
                        .map(existing -> {
                            existing.apply(profile);
                            return existing;
                        })
                        .orElseGet(() -> ServiceProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId())));
        return serviceProfileJpaRepository.save(entity).toModel(profile.getTenant());
    }
}
