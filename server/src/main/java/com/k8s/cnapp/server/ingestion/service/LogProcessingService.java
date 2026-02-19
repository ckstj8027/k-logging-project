package com.k8s.cnapp.server.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.server.ingestion.dto.ClusterSnapshot;
import com.k8s.cnapp.server.profile.domain.AssetContext;
import com.k8s.cnapp.server.profile.domain.Profile;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogProcessingService {

    private final ObjectMapper objectMapper;
    // private final ProfileRepository profileRepository; // 추후 저장 시 필요

    @Transactional
    public void processRawData(String rawData) {
        try {
            // 1. JSON 역직렬화
            ClusterSnapshot snapshot = objectMapper.readValue(rawData, ClusterSnapshot.class);
            
            // 2. 로그 요약 출력
            logSummary(snapshot);

            // 3. Profile 변환 및 처리
            List<Profile> profiles = convertToProfiles(snapshot.pods());
            
            // 4. (Optional) 저장 또는 BaselineService 전달
            // profileRepository.saveAll(profiles);
            log.info("Converted {} profiles from snapshot.", profiles.size());

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

    private List<Profile> convertToProfiles(List<V1Pod> pods) {
        if (pods == null || pods.isEmpty()) {
            return Collections.emptyList();
        }

        List<Profile> profiles = new ArrayList<>();
        for (V1Pod pod : pods) {
            if (pod.getMetadata() == null || pod.getSpec() == null) {
                continue;
            }

            String namespace = pod.getMetadata().getNamespace();
            String podName = pod.getMetadata().getName();
            
            // 첫 번째 컨테이너 정보 추출
            List<V1Container> containers = pod.getSpec().getContainers();
            if (containers == null || containers.isEmpty()) {
                continue;
            }
            V1Container container = containers.get(0);
            String containerName = container.getName();
            String image = container.getImage();
            
            // Deployment 이름 추론 (단순화된 로직: podName에서 마지막 하이픈 뒤 제거)
            // 실제로는 OwnerReferences를 확인하는 것이 정확함
            String deploymentName = extractDeploymentName(podName);

            AssetContext assetContext = new AssetContext(namespace, podName, containerName, image, deploymentName);
            
            // Feature는 현재 비어있는 상태로 초기화 (추후 메트릭 추가 가능)
            Map<String, Double> features = Collections.emptyMap();

            // 기본값으로 LEARNING 모드 설정
            Profile profile = new Profile(assetContext, features, Profile.ProfileType.LEARNING);
            profiles.add(profile);
        }
        return profiles;
    }

    private String extractDeploymentName(String podName) {
        if (podName == null) return null;
        // 예: my-app-7d8f9c-abcde -> my-app
        // 간단하게 마지막 두 개의 하이픈 세그먼트를 제거하는 방식 (ReplicaSet hash + Pod hash)
        // 하지만 정확하지 않을 수 있으므로 여기서는 null로 두거나 단순 처리
        // 일단은 null로 처리하고 필요 시 로직 고도화
        return null; 
    }
}
