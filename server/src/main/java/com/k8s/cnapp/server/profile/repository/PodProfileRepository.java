package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.PodProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PodProfileRepository extends JpaRepository<PodProfile, Long> {

    // 특정 자산(Namespace/Deployment)의 프로필 조회 (Type 필드 제거됨)
    List<PodProfile> findByAssetContext_NamespaceAndAssetContext_DeploymentName(
            String namespace, String deploymentName);

    // 중복 체크 및 Upsert를 위한 조회 메서드
    Optional<PodProfile> findByAssetContext_NamespaceAndAssetContext_PodNameAndAssetContext_ContainerName(
            String namespace, String podName, String containerName);

    // In-Clause Batch Fetching을 위한 메서드
    // 복합 키 조회를 위해 JPQL 사용 (CONCAT으로 키 생성하여 비교)
    // 주의: DB에 따라 CONCAT 성능 이슈가 있을 수 있으나, 현재 구조에서 가장 간단한 방법
    @Query("SELECT p FROM PodProfile p WHERE CONCAT(p.assetContext.namespace, '/', p.assetContext.podName, '/', p.assetContext.containerName) IN :keys")
    List<PodProfile> findAllByKeys(@Param("keys") List<String> keys);
}
