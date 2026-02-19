package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.NamespaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NamespaceProfileRepository extends JpaRepository<NamespaceProfile, Long> {
    Optional<NamespaceProfile> findByName(String name);
}
