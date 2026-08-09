package com.k8s.cnapp.msa.query.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface EventProfileJpaRepository extends JpaRepository<EventProfileEntity, Long> {
    List<EventProfileEntity> findAllByTenantId(Long tenantId);

    List<EventProfileEntity> findByTenantIdOrderByIdDesc(Long tenantId, Pageable pageable);

    List<EventProfileEntity> findByTenantIdAndIdLessThanOrderByIdDesc(Long tenantId, Long id, Pageable pageable);
}
