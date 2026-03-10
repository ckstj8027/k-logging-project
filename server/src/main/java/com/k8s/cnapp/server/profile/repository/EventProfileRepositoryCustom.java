package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.EventProfile;

import java.util.List;

public interface EventProfileRepositoryCustom {
    List<EventProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize);
}
