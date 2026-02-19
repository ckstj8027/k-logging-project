package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {
    Optional<ServiceProfile> findByNamespaceAndName(String namespace, String name);
}
