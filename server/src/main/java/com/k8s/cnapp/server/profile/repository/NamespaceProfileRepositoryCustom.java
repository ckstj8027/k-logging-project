package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.NamespaceProfile;

import java.util.List;

public interface NamespaceProfileRepositoryCustom {
    List<NamespaceProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize);
}
