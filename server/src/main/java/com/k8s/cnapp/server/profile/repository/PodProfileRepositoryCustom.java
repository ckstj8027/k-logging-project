package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.PodProfile;

import java.util.List;

public interface PodProfileRepositoryCustom {
    List<PodProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize);
}
