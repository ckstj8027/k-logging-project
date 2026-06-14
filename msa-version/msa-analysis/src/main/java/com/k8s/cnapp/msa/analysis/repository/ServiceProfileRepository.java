package com.k8s.cnapp.msa.analysis.repository;

import com.k8s.cnapp.msa.analysis.domain.ServiceProfile;
import com.k8s.cnapp.msa.analysis.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {
    Optional<ServiceProfile> findByTenantAndNamespaceAndName(Tenant tenant, String namespace, String name);
}
