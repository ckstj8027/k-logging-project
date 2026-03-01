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
public interface NamespaceProfileRepository extends JpaRepository<NamespaceProfile, Long> {

    List<NamespaceProfile> findAllByTenant(Tenant tenant);

    List<NamespaceProfile> findByTenantAndNameIn(Tenant tenant, List<String> names);

    // 스냅샷에 없는 데이터 삭제 (Tenant 격리)
    @Modifying
    @Query("DELETE FROM NamespaceProfile n WHERE n.tenant = :tenant AND n.name NOT IN :names")
    void deleteByTenantAndNameNotIn(@Param("tenant") Tenant tenant, @Param("names") List<String> names);
}
