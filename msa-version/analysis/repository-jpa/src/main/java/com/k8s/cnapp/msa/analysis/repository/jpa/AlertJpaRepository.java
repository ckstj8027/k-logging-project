package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.common.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface AlertJpaRepository extends JpaRepository<AlertEntity, Long> {
    List<AlertEntity> findByTenantAndStatus(TenantEntity tenant, Status status);

    boolean existsByTenantAndResourceTypeAndResourceNameAndMessageAndStatus(
            TenantEntity tenant, String resourceType, String resourceName, String message, Status status);
}
