package com.k8s.cnapp.msa.analysis.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface DeploymentProfileJpaRepository extends JpaRepository<DeploymentProfileEntity, Long> {
    Optional<DeploymentProfileEntity> findByTenantAndNamespaceAndName(TenantEntity tenant, String namespace, String name);
}
