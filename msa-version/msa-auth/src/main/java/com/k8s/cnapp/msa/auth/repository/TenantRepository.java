package com.k8s.cnapp.msa.auth.repository;

import com.k8s.cnapp.msa.auth.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
}
