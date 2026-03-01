package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentProfileRepository extends JpaRepository<DeploymentProfile, Long> {

    List<DeploymentProfile> findAllByTenant(Tenant tenant);

    // In-Clause Batch Fetching (Tenant 격리)
    @Query("SELECT d FROM DeploymentProfile d WHERE d.tenant = :tenant AND CONCAT(d.namespace, '/', d.name) IN :keys")
    List<DeploymentProfile> findAllByTenantAndKeys(@Param("tenant") Tenant tenant, @Param("keys") List<String> keys);

    // 스냅샷에 없는 데이터 삭제 (Tenant 격리)
    @Modifying
    @Query("DELETE FROM DeploymentProfile d WHERE d.tenant = :tenant AND CONCAT(d.namespace, '/', d.name) NOT IN :keys")
    void deleteByTenantAndKeysNotIn(@Param("tenant") Tenant tenant, @Param("keys") List<String> keys);
}
