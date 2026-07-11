package com.k8s.cnapp.msa.query.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PodProfileJpaRepository extends JpaRepository<PodProfileEntity, Long> {
    List<PodProfileEntity> findAllByTenantId(Long tenantId);
}
