package com.k8s.cnapp.msa.analysis.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PolicyJpaRepository extends JpaRepository<PolicyEntity, Long> {
    List<PolicyEntity> findAllByTenant(TenantEntity tenant);
}
