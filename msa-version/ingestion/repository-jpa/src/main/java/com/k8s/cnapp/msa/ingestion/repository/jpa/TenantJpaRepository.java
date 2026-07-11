package com.k8s.cnapp.msa.ingestion.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface TenantJpaRepository extends JpaRepository<TenantEntity, Long> {
    Optional<TenantEntity> findByApiKey(String apiKey);
}
