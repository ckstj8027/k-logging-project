package com.k8s.cnapp.server.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.server.ingestion.dto.ClusterSnapshot;
import com.k8s.cnapp.server.profile.domain.AssetContext;
import com.k8s.cnapp.server.profile.domain.Profile;
import com.k8s.cnapp.server.profile.repository.ProfileRepository;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogProcessingService {

    private final ObjectMapper objectMapper;
    private final ProfileRepository profileRepository;

    @Transactional
    public void processRawData(String rawData) {
        try {
            // 1. JSON 역직렬화
            ClusterSnapshot snapshot = objectMapper.readValue(rawData, ClusterSnapshot.class);
            
            // 2. 로그 요약 출력
            logSummary(snapshot);

            // 3. Profile 변환 및 Upsert 처리
            processProfiles(snapshot);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse raw data to ClusterSnapshot", e);
        }
    }

    private void logSummary(ClusterSnapshot snapshot) {
        int podCount = snapshot.pods() != null ? snapshot.pods().size() : 0;
        int nodeCount = snapshot.nodes() != null ? snapshot.nodes().size() : 0;
        int serviceCount = snapshot.services() != null ? snapshot.services().size() : 0;
        int deploymentCount = snapshot.deployments() != null ? snapshot.deployments().size() : 0;
        
        log.info("[Ingest] Received Snapshot (Pods: {}, Nodes: {}, Services: {}, Deployments: {})", 
                podCount, nodeCount, serviceCount, deploymentCount);
    }

    private void processProfiles(ClusterSnapshot snapshot) {
        List<V1Pod> pods = snapshot.pods();
        if (pods == null || pods.isEmpty()) {
            return;
        }

        int savedCount = 0;
        int updatedCount = 0;

        for (V1Pod pod : pods) {
            if (pod.getMetadata() == null || pod.getSpec() == null) {
                continue;
            }

            String namespace = pod.getMetadata().getNamespace();
            String podName = pod.getMetadata().getName();
            
            // 첫 번째 컨테이너 정보 추출 (멀티 컨테이너 지원 필요 시 루프 필요)
            List<V1Container> containers = pod.getSpec().getContainers();
            if (containers == null || containers.isEmpty()) {
                continue;
            }
            V1Container container = containers.get(0);
            String containerName = container.getName();
            String image = container.getImage();
            
            // Deployment 이름 추출 (OwnerReference 활용)
            String deploymentName = extractDeploymentName(pod);

            // DB 조회 (Upsert 로직)
            Optional<Profile> existingProfileOpt = profileRepository
                    .findByAssetContext_NamespaceAndAssetContext_PodNameAndAssetContext_ContainerName(
                            namespace, podName, containerName);

            if (existingProfileOpt.isPresent()) {
                // UPDATE: 이미지가 변경되었거나 Deployment 정보가 업데이트 되었을 수 있음
                Profile existingProfile = existingProfileOpt.get();
                
                // 변경 사항이 있을 때만 업데이트 (Dirty Checking)
                boolean isChanged = false;
                if (!Objects.equals(existingProfile.getAssetContext().getImage(), image)) {
                    isChanged = true;
                }
                if (!Objects.equals(existingProfile.getAssetContext().getDeploymentName(), deploymentName)) {
                    isChanged = true;
                }

                if (isChanged) {
                    AssetContext newContext = new AssetContext(namespace, podName, containerName, image, deploymentName);
                    existingProfile.updateAssetContext(newContext);
                    updatedCount++;
                }
                // lastSeen 업데이트 효과 (JPA Auditing @UpdateTimestamp)
            } else {
                // INSERT: 새로운 Pod 발견
                AssetContext assetContext = new AssetContext(namespace, podName, containerName, image, deploymentName);
                Map<String, Double> features = Collections.emptyMap();
                Profile newProfile = new Profile(assetContext, features, Profile.ProfileType.LEARNING);
                profileRepository.save(newProfile);
                savedCount++;
            }
        }
        
        log.info("Processed Profiles: New={}, Updated={}", savedCount, updatedCount);
    }

    private String extractDeploymentName(V1Pod pod) {
        if (pod.getMetadata().getOwnerReferences() == null) {
            return null;
        }

        for (V1OwnerReference owner : pod.getMetadata().getOwnerReferences()) {
            // 1. Deployment가 직접 Owner인 경우 (드물지만 가능)
            if ("Deployment".equals(owner.getKind())) {
                return owner.getName();
            }
            // 2. ReplicaSet이 Owner인 경우 (일반적인 Deployment 패턴)
            else if ("ReplicaSet".equals(owner.getKind())) {
                // ReplicaSet 이름에서 Deployment 이름 유추 (보통 rsName은 deployName-hash 형태)
                String rsName = owner.getName();
                int lastHyphen = rsName.lastIndexOf('-');
                if (lastHyphen > 0) {
                    return rsName.substring(0, lastHyphen);
                }
                return rsName;
            }
            // 3. DaemonSet, StatefulSet
            else if ("DaemonSet".equals(owner.getKind()) || "StatefulSet".equals(owner.getKind())) {
                return owner.getName();
            }
        }
        return null;
    }
}
