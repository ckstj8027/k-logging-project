package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.asset.EventProfileStore;
import com.k8s.cnapp.msa.analysis.model.EventProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class EventProfileStoreJpaAdapter implements EventProfileStore {

    private final EventProfileJpaRepository eventProfileJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public Optional<EventProfile> findByTenantAndUid(Tenant tenant, String uid) {
        return eventProfileJpaRepository
                .findByTenantAndUid(tenantJpaRepository.getReferenceById(tenant.getId()), uid)
                .map(entity -> entity.toModel(tenant));
    }

    @Override
    public EventProfile save(EventProfile profile) {
        EventProfileEntity entity = (profile.getId() == null)
                ? EventProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId()))
                : eventProfileJpaRepository.findById(profile.getId())
                        .map(existing -> {
                            existing.apply(profile);
                            return existing;
                        })
                        .orElseGet(() -> EventProfileEntity.fromModel(profile, tenantJpaRepository.getReferenceById(profile.getTenant().getId())));
        return eventProfileJpaRepository.save(entity).toModel(profile.getTenant());
    }
}
