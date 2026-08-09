package com.k8s.cnapp.msa.query.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface NamespaceProfileJpaRepository extends JpaRepository<NamespaceProfileEntity, Long> {
    List<NamespaceProfileEntity> findAllByTenantId(Long tenantId);

    List<NamespaceProfileEntity> findByTenantIdOrderByIdDesc(Long tenantId, Pageable pageable);

    List<NamespaceProfileEntity> findByTenantIdAndIdLessThanOrderByIdDesc(Long tenantId, Long id, Pageable pageable);
}
