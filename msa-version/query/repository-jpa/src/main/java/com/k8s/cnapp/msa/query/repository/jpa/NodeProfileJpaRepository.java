package com.k8s.cnapp.msa.query.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface NodeProfileJpaRepository extends JpaRepository<NodeProfileEntity, Long> {
    List<NodeProfileEntity> findAllByTenantId(Long tenantId);
}
