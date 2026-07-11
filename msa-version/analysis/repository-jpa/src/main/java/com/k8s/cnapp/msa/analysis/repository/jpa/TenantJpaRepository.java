package com.k8s.cnapp.msa.analysis.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

interface TenantJpaRepository extends JpaRepository<TenantEntity, Long> {
}
