package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // 특정 자산(Namespace/Deployment)의 학습용 프로필 조회
    List<Profile> findByAssetContext_NamespaceAndAssetContext_DeploymentNameAndType(
            String namespace, String deploymentName, Profile.ProfileType type);

    // 중복 체크 및 Upsert를 위한 조회 메서드
    Optional<Profile> findByAssetContext_NamespaceAndAssetContext_PodNameAndAssetContext_ContainerName(
            String namespace, String podName, String containerName);
}
