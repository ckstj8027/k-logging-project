package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {
    Optional<ServiceProfile> findByNamespaceAndName(String namespace, String name);

    // N+1 문제 해결을 위한 Fetch Join
    @Query("SELECT DISTINCT s FROM ServiceProfile s LEFT JOIN FETCH s.ports")
    List<ServiceProfile> findAllWithPorts();

    // In-Clause Batch Fetching + Fetch Join
    @Query("SELECT DISTINCT s FROM ServiceProfile s LEFT JOIN FETCH s.ports WHERE CONCAT(s.namespace, '/', s.name) IN :keys")
    List<ServiceProfile> findAllByKeysWithPorts(@Param("keys") List<String> keys);

    // 스냅샷에 없는 데이터 삭제 (동기화)
    @Modifying
    @Query("DELETE FROM ServiceProfile s WHERE CONCAT(s.namespace, '/', s.name) NOT IN :keys")
    void deleteByKeysNotIn(@Param("keys") List<String> keys);
}
