package com.k8s.cnapp.msa.query.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface NodeProfileJpaRepository extends JpaRepository<NodeProfileEntity, Long> {
    List<NodeProfileEntity> findAllByTenantId(Long tenantId);

    List<NodeProfileEntity> findByTenantIdOrderByIdDesc(Long tenantId, Pageable pageable);

    List<NodeProfileEntity> findByTenantIdAndIdLessThanOrderByIdDesc(Long tenantId, Long id, Pageable pageable);
}
