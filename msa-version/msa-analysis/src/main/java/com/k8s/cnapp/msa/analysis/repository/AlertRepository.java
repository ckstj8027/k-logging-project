package com.k8s.cnapp.msa.analysis.repository;

import com.k8s.cnapp.msa.analysis.domain.Alert;
import com.k8s.cnapp.msa.analysis.domain.Tenant;
import com.k8s.cnapp.msa.common.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByTenantAndStatus(Tenant tenant, Status status);
}
