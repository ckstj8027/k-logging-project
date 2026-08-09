package com.k8s.cnapp.msa.query.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DeploymentProfileJpaRepository extends JpaRepository<DeploymentProfileEntity, Long> {
    List<DeploymentProfileEntity> findAllByTenantId(Long tenantId);

    List<DeploymentProfileEntity> findByTenantIdOrderByIdDesc(Long tenantId, Pageable pageable);

    List<DeploymentProfileEntity> findByTenantIdAndIdLessThanOrderByIdDesc(Long tenantId, Long id, Pageable pageable);
}
