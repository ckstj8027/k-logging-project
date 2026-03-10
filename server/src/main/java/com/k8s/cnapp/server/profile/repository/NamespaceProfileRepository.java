package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.NamespaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NamespaceProfileRepository extends JpaRepository<NamespaceProfile, Long>, NamespaceProfileRepositoryCustom {

    List<NamespaceProfile> findAllByTenant(Tenant tenant);

    List<NamespaceProfile> findByTenantAndNameIn(Tenant tenant, List<String> names);

    @Modifying
    void deleteAllByLastSeenAtBefore(java.time.LocalDateTime time);
}
