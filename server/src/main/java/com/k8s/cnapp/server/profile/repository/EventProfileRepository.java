package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.EventProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventProfileRepository extends JpaRepository<EventProfile, Long> {

    List<EventProfile> findAllByTenant(Tenant tenant);

    List<EventProfile> findByTenantAndUidIn(Tenant tenant, List<String> uids);

    @Modifying
    void deleteAllByLastSeenAtBefore(java.time.LocalDateTime time);
}
