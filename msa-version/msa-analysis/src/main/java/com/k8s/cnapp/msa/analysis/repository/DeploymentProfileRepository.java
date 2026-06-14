package com.k8s.cnapp.msa.analysis.repository;

import com.k8s.cnapp.msa.analysis.domain.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeploymentProfileRepository extends JpaRepository<DeploymentProfile, Long> {
    Optional<DeploymentProfile> findByTenantAndNamespaceAndName(Tenant tenant, String namespace, String name);
}
