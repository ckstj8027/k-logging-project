package com.k8s.cnapp.msa.query.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ServiceProfileJpaRepository extends JpaRepository<ServiceProfileEntity, Long> {
    List<ServiceProfileEntity> findAllByTenantId(Long tenantId);
}
