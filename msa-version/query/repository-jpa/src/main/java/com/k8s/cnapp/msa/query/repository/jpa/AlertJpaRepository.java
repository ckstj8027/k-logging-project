package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.common.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface AlertJpaRepository extends JpaRepository<AlertEntity, Long> {
    List<AlertEntity> findAllByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, Status status);
}
