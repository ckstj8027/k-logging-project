package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.PodProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PodProfileRepository extends JpaRepository<PodProfile, Long> {

    List<PodProfile> findAllByTenant(Tenant tenant);

    // In-Clause Batch Fetching (Tenant 격리)
    @Query("SELECT p FROM PodProfile p WHERE p.tenant = :tenant AND CONCAT(p.assetContext.namespace, '/', p.assetContext.podName, '/', p.assetContext.containerName) IN :keys")
    List<PodProfile> findAllByTenantAndKeys(@Param("tenant") Tenant tenant, @Param("keys") List<String> keys);

    // GC용: 일정 시간 동안 생존 보고가 없는 데이터 삭제
    @Modifying
    void deleteAllByLastSeenAtBefore(java.time.LocalDateTime time);
}
