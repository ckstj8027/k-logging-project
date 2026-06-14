package com.k8s.cnapp.msa.analysis.repository;

import com.k8s.cnapp.msa.analysis.domain.NodeProfile;
import com.k8s.cnapp.msa.analysis.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NodeProfileRepository extends JpaRepository<NodeProfile, Long> {
    Optional<NodeProfile> findByTenantAndName(Tenant tenant, String name);
}
