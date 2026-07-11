package com.k8s.cnapp.msa.analysis.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ServiceProfileJpaRepository extends JpaRepository<ServiceProfileEntity, Long> {
    Optional<ServiceProfileEntity> findByTenantAndNamespaceAndName(TenantEntity tenant, String namespace, String name);
}
