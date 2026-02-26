package com.k8s.cnapp.server.alert.repository;

import com.k8s.cnapp.server.alert.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    // 중복 Alert 방지를 위해 OPEN 상태인 동일 리소스의 Alert 조회
    List<Alert> findByResourceTypeAndResourceNameAndStatus(String resourceType, String resourceName, Alert.Status status);

    // OPEN 상태인 모든 Alert 조회 (벌크 조회용)
    List<Alert> findByStatus(Alert.Status status);

    // In-Clause Batch Fetching을 위한 메서드
    // 특정 리소스 이름들에 대한 OPEN 상태 Alert 조회
    @Query("SELECT a FROM Alert a WHERE a.status = :status AND a.resourceName IN :resourceNames")
    List<Alert> findByStatusAndResourceNameIn(@Param("status") Alert.Status status, @Param("resourceNames") List<String> resourceNames);
}
