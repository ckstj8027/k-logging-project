package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {

    // N+1 문제 해결을 위한 Fetch Join (Tenant 격리)
    @Query("SELECT DISTINCT s FROM ServiceProfile s LEFT JOIN FETCH s.ports WHERE s.tenant = :tenant")
    List<ServiceProfile> findAllByTenantWithPorts(@Param("tenant") Tenant tenant);

    // In-Clause Batch Fetching + Fetch Join (Tenant 격리)
    @Query("SELECT DISTINCT s FROM ServiceProfile s LEFT JOIN FETCH s.ports WHERE s.tenant = :tenant AND CONCAT(s.namespace, '/', s.name) IN :keys")
    List<ServiceProfile> findAllByTenantAndKeysWithPorts(@Param("tenant") Tenant tenant, @Param("keys") List<String> keys);

    // 스냅샷에 없는 데이터 삭제 (Tenant 격리)
    @Modifying
    @Query("DELETE FROM ServiceProfile s WHERE s.tenant = :tenant AND CONCAT(s.namespace, '/', s.name) NOT IN :keys")
    void deleteByTenantAndKeysNotIn(@Param("tenant") Tenant tenant, @Param("keys") List<String> keys);
}
