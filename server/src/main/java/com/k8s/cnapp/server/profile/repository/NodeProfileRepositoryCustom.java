package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.NodeProfile;

import java.util.List;

public interface NodeProfileRepositoryCustom {
    List<NodeProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize);
}
