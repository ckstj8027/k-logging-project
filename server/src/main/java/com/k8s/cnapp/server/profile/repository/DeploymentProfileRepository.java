package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentProfileRepository extends JpaRepository<DeploymentProfile, Long> {
    Optional<DeploymentProfile> findByNamespaceAndName(String namespace, String name);

    @Query("SELECT d FROM DeploymentProfile d WHERE CONCAT(d.namespace, '/', d.name) IN :keys")
    List<DeploymentProfile> findAllByKeys(@Param("keys") List<String> keys);

    // 스냅샷에 없는 데이터 삭제 (동기화)
    @Modifying
    @Query("DELETE FROM DeploymentProfile d WHERE CONCAT(d.namespace, '/', d.name) NOT IN :keys")
    void deleteByKeysNotIn(@Param("keys") List<String> keys);
}
