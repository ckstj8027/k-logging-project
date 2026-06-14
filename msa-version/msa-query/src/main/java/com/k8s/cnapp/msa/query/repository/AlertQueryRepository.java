package com.k8s.cnapp.msa.query.repository;

import com.k8s.cnapp.msa.query.domain.Alert;
import com.k8s.cnapp.msa.common.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertQueryRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, Status status);
}
