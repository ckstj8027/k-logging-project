package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {
    Optional<ServiceProfile> findByNamespaceAndName(String namespace, String name);

    // N+1 문제 해결을 위한 Fetch Join
    @Query("SELECT DISTINCT s FROM ServiceProfile s LEFT JOIN FETCH s.ports")
    List<ServiceProfile> findAllWithPorts();
}
