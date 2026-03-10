package com.k8s.cnapp.server.alert.repository;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.auth.domain.Tenant;

import java.util.List;

public interface AlertRepositoryCustom {
    List<Alert> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize);
}
