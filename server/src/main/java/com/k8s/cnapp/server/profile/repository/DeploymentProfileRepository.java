package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeploymentProfileRepository extends JpaRepository<DeploymentProfile, Long> {
    Optional<DeploymentProfile> findByNamespaceAndName(String namespace, String name);
}
