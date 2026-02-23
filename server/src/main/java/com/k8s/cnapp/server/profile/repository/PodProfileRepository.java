package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.PodProfile;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
