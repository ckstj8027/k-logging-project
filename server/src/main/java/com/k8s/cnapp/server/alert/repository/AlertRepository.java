package com.k8s.cnapp.server.alert.repository;

import com.k8s.cnapp.server.alert.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    // 중복 Alert 방지를 위해 OPEN 상태인 동일 리소스의 Alert 조회
    List<Alert> findByResourceTypeAndResourceNameAndStatus(String resourceType, String resourceName, Alert.Status status);

    // OPEN 상태인 모든 Alert 조회 (벌크 조회용)
    List<Alert> findByStatus(Alert.Status status);
}
