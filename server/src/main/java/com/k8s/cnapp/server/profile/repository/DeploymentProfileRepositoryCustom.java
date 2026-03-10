package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.DeploymentProfile;

import java.util.List;

public interface DeploymentProfileRepositoryCustom {
    List<DeploymentProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize);
}
