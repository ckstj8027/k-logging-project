package com.k8s.cnapp.server.policy.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.policy.domain.Policy;

import java.util.List;

public interface PolicyRepositoryCustom {
    List<Policy> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize);
}
