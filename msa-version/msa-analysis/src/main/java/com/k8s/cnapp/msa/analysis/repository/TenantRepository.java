package com.k8s.cnapp.msa.analysis.repository;

import com.k8s.cnapp.msa.analysis.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
