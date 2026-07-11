package com.k8s.cnapp.msa.analysis.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface NodeProfileJpaRepository extends JpaRepository<NodeProfileEntity, Long> {
    Optional<NodeProfileEntity> findByTenantAndName(TenantEntity tenant, String name);
}
