package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.NodeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeProfileRepository extends JpaRepository<NodeProfile, Long> {
    Optional<NodeProfile> findByName(String name);

    List<NodeProfile> findByNameIn(List<String> names);
}
