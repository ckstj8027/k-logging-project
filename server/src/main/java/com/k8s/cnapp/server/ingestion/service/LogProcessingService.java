package com.k8s.cnapp.server.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.server.ingestion.dto.ClusterSnapshot;
import com.k8s.cnapp.server.profile.domain.*;
import com.k8s.cnapp.server.profile.repository.*;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogProcessingService {

    private final ObjectMapper objectMapper;
    private final ProfileRepository profileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final NamespaceProfileRepository namespaceProfileRepository;
    private final EventProfileRepository eventProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;

    /**
     * 에이전트로부터 수신한 원본 로그(JSON)를 처리하는 메인 메서드
     * 1. JSON 파싱 (ClusterSnapshot DTO 변환)
     * 2. 각 리소스별(Pod, Service, Node 등) 처리 로직 호출
     */
    @Transactional
    public void processRawData(String rawData) {
        try {
            // 1. JSON 역직렬화
            ClusterSnapshot snapshot = objectMapper.readValue(rawData, ClusterSnapshot.class);
            
            // 2. 로그 요약 출력
            logSummary(snapshot);

            // 3. 각 리소스별 처리 (Upsert 로직 포함)
            processPods(snapshot.pods());
            processServices(snapshot.services());
            processNodes(snapshot.nodes());
            processNamespaces(snapshot.namespaces());
            processEvents(snapshot.events());
            processDeployments(snapshot.deployments());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse raw data to ClusterSnapshot", e);
        }
    }

    /**
     * 수신된 스냅샷의 요약 정보를 로그로 출력
     */
    private void logSummary(ClusterSnapshot snapshot) {
        log.info("[Ingest] Received Snapshot (Pods: {}, Services: {}, Nodes: {}, Namespaces: {}, Events: {}, Deployments: {})",
                snapshot.pods() != null ? snapshot.pods().size() : 0,
                snapshot.services() != null ? snapshot.services().size() : 0,
                snapshot.nodes() != null ? snapshot.nodes().size() : 0,
                snapshot.namespaces() != null ? snapshot.namespaces().size() : 0,
                snapshot.events() != null ? snapshot.events().size() : 0,
                snapshot.deployments() != null ? snapshot.deployments().size() : 0
        );
    }

    /**
     * Pod 정보를 처리하여 Profile 엔티티로 저장 또는 업데이트
     * - 중복 방지: Namespace + PodName + ContainerName 조합으로 기존 데이터 확인
     * - 변경 감지: 이미지나 Deployment 정보가 변경된 경우에만 업데이트
     */
    private void processPods(List<V1Pod> pods) {
        if (pods == null) return;
        int newCount = 0, updatedCount = 0;
        for (V1Pod pod : pods) {
            if (pod.getMetadata() == null || pod.getSpec() == null) continue;
            String namespace = pod.getMetadata().getNamespace();
            String podName = pod.getMetadata().getName();
            
            // 컨테이너가 없는 경우 스킵
            if (pod.getSpec().getContainers() == null || pod.getSpec().getContainers().isEmpty()) continue;

            // 첫 번째 컨테이너 정보만 처리 (추후 멀티 컨테이너 지원 필요)
            V1Container container = pod.getSpec().getContainers().get(0);
            String containerName = container.getName();
            String image = container.getImage();
            String deploymentName = extractDeploymentName(pod);

            // DB 조회 (Upsert 로직)
            Optional<Profile> existing = profileRepository.findByAssetContext_NamespaceAndAssetContext_PodNameAndAssetContext_ContainerName(namespace, podName, containerName);
            if (existing.isPresent()) {
                Profile p = existing.get();
                // 변경 사항이 있을 때만 업데이트 (Dirty Checking)
                if (!Objects.equals(p.getAssetContext().getImage(), image) || !Objects.equals(p.getAssetContext().getDeploymentName(), deploymentName)) {
                    p.updateAssetContext(new AssetContext(namespace, podName, containerName, image, deploymentName));
                    updatedCount++;
                }
            } else {
                // 신규 생성
                profileRepository.save(new Profile(new AssetContext(namespace, podName, containerName, image, deploymentName), Collections.emptyMap(), Profile.ProfileType.LEARNING));
                newCount++;
            }
        }
        log.info("Processed Pods: New={}, Updated={}", newCount, updatedCount);
    }

    /**
     * Service 정보를 처리하여 ServiceProfile 엔티티로 저장 또는 업데이트
     */
    private void processServices(List<V1Service> services) {
        if (services == null) return;
        int newCount = 0, updatedCount = 0;
        for (V1Service service : services) {
            if (service.getMetadata() == null || service.getSpec() == null) continue;
            String namespace = service.getMetadata().getNamespace();
            String name = service.getMetadata().getName();
            String type = service.getSpec().getType();
            String clusterIp = service.getSpec().getClusterIP();
            String externalIps = service.getSpec().getExternalIPs() != null ? String.join(",", service.getSpec().getExternalIPs()) : null;
            String portsJson = toJson(service.getSpec().getPorts());

            Optional<ServiceProfile> existing = serviceProfileRepository.findByNamespaceAndName(namespace, name);
            if (existing.isPresent()) {
                ServiceProfile sp = existing.get();
                // 변경 감지: 타입, ClusterIP, ExternalIPs, Ports 정보가 변경된 경우에만 업데이트
                boolean isChanged = !Objects.equals(sp.getType(), type) ||
                                    !Objects.equals(sp.getClusterIp(), clusterIp) ||
                                    !Objects.equals(sp.getExternalIps(), externalIps) ||
                                    !Objects.equals(sp.getPortsJson(), portsJson);
                
                if (isChanged) {
                    sp.update(type, clusterIp, externalIps, portsJson);
                    updatedCount++;
                }
            } else {
                serviceProfileRepository.save(new ServiceProfile(namespace, name, type, clusterIp, externalIps, portsJson));
                newCount++;
            }
        }
        log.info("Processed Services: New={}, Updated={}", newCount, updatedCount);
    }

    /**
     * Node 정보를 처리하여 NodeProfile 엔티티로 저장 또는 업데이트
     */
    private void processNodes(List<V1Node> nodes) {
        if (nodes == null) return;
        int newCount = 0, updatedCount = 0;
        for (V1Node node : nodes) {
            if (node.getMetadata() == null || node.getStatus() == null) continue;
            String name = node.getMetadata().getName();
            V1NodeSystemInfo nodeInfo = node.getStatus().getNodeInfo();
            String osImage = nodeInfo.getOsImage();
            String kernelVersion = nodeInfo.getKernelVersion();
            String containerRuntimeVersion = nodeInfo.getContainerRuntimeVersion();
            String kubeletVersion = nodeInfo.getKubeletVersion();
            
            // Capacity 정보가 없을 수 있음 (예외 처리)
            String cpu = node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("cpu") != null 
                    ? node.getStatus().getCapacity().get("cpu").toSuffixedString() : "unknown";
            String memory = node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("memory") != null 
                    ? node.getStatus().getCapacity().get("memory").toSuffixedString() : "unknown";

            Optional<NodeProfile> existing = nodeProfileRepository.findByName(name);
            if (existing.isPresent()) {
                NodeProfile np = existing.get();
                // 변경 감지: OS 이미지, 커널 버전, 런타임 버전 등이 변경된 경우에만 업데이트
                boolean isChanged = !Objects.equals(np.getOsImage(), osImage) ||
                                    !Objects.equals(np.getKernelVersion(), kernelVersion) ||
                                    !Objects.equals(np.getContainerRuntimeVersion(), containerRuntimeVersion) ||
                                    !Objects.equals(np.getKubeletVersion(), kubeletVersion) ||
                                    !Objects.equals(np.getCpuCapacity(), cpu) ||
                                    !Objects.equals(np.getMemoryCapacity(), memory);

                if (isChanged) {
                    np.update(osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory);
                    updatedCount++;
                }
            } else {
                nodeProfileRepository.save(new NodeProfile(name, osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory));
                newCount++;
            }
        }
        log.info("Processed Nodes: New={}, Updated={}", newCount, updatedCount);
    }

    /**
     * Namespace 정보를 처리하여 NamespaceProfile 엔티티로 저장 또는 업데이트
     */
    private void processNamespaces(List<V1Namespace> namespaces) {
        if (namespaces == null) return;
        int newCount = 0, updatedCount = 0;
        for (V1Namespace ns : namespaces) {
            if (ns.getMetadata() == null || ns.getStatus() == null) continue;
            String name = ns.getMetadata().getName();
            String status = ns.getStatus().getPhase();

            Optional<NamespaceProfile> existing = namespaceProfileRepository.findByName(name);
            if (existing.isPresent()) {
                NamespaceProfile nsp = existing.get();
                // 변경 감지: 상태(Status)가 변경된 경우에만 업데이트
                if (!Objects.equals(nsp.getStatus(), status)) {
                    nsp.update(status);
                    updatedCount++;
                }
            } else {
                namespaceProfileRepository.save(new NamespaceProfile(name, status));
                newCount++;
            }
        }
        log.info("Processed Namespaces: New={}, Updated={}", newCount, updatedCount);
    }

    /**
     * Event 정보를 처리하여 EventProfile 엔티티로 저장 또는 업데이트
     * - UID를 기준으로 중복 여부 확인
     */
    private void processEvents(List<CoreV1Event> events) {
        if (events == null) return;
        int newCount = 0, updatedCount = 0;
        for (CoreV1Event event : events) {
            if (event.getMetadata() == null || event.getInvolvedObject() == null) continue;
            String uid = event.getMetadata().getUid();
            Integer count = event.getCount();
            OffsetDateTime lastTimestamp = event.getLastTimestamp();
            String message = event.getMessage();

            Optional<EventProfile> existing = eventProfileRepository.findByUid(uid);
            if (existing.isPresent()) {
                EventProfile ep = existing.get();
                // 변경 감지: Count가 증가했거나 메시지가 변경된 경우에만 업데이트
                if (!Objects.equals(ep.getCount(), count)) {
                    ep.update(count, lastTimestamp, message);
                    updatedCount++;
                }
            } else {
                eventProfileRepository.save(new EventProfile(
                        event.getMetadata().getNamespace(),
                        event.getInvolvedObject().getKind(),
                        event.getInvolvedObject().getName(),
                        event.getReason(),
                        message,
                        event.getType(),
                        lastTimestamp,
                        count,
                        uid
                ));
                newCount++;
            }
        }
        log.info("Processed Events: New={}, Updated={}", newCount, updatedCount);
    }
    
    /**
     * Deployment 정보를 처리하여 DeploymentProfile 엔티티로 저장 또는 업데이트
     */
    private void processDeployments(List<V1Deployment> deployments) {
        if (deployments == null) return;
        int newCount = 0, updatedCount = 0;
        for (V1Deployment dep : deployments) {
            if (dep.getMetadata() == null || dep.getSpec() == null) continue;
            String namespace = dep.getMetadata().getNamespace();
            String name = dep.getMetadata().getName();
            Integer replicas = dep.getSpec().getReplicas();
            Integer availableReplicas = dep.getStatus() != null ? dep.getStatus().getAvailableReplicas() : 0;
            String strategy = dep.getSpec().getStrategy() != null ? dep.getSpec().getStrategy().getType() : null;
            String selectorJson = toJson(dep.getSpec().getSelector());

            Optional<DeploymentProfile> existing = deploymentProfileRepository.findByNamespaceAndName(namespace, name);
            if (existing.isPresent()) {
                DeploymentProfile dp = existing.get();
                // 변경 감지: Replicas, Strategy, Selector 등이 변경된 경우에만 업데이트
                boolean isChanged = !Objects.equals(dp.getReplicas(), replicas) ||
                                    !Objects.equals(dp.getAvailableReplicas(), availableReplicas) ||
                                    !Objects.equals(dp.getStrategyType(), strategy) ||
                                    !Objects.equals(dp.getSelectorJson(), selectorJson);

                if (isChanged) {
                    dp.update(replicas, availableReplicas, strategy, selectorJson);
                    updatedCount++;
                }
            } else {
                deploymentProfileRepository.save(new DeploymentProfile(namespace, name, replicas, availableReplicas, strategy, selectorJson));
                newCount++;
            }
        }
        log.info("Processed Deployments: New={}, Updated={}", newCount, updatedCount);
    }

    /**
     * Pod의 OwnerReferences를 분석하여 Deployment 이름을 추출
     * - ReplicaSet인 경우: 이름 규칙(deployName-hash)을 통해 유추
     * - Deployment, DaemonSet, StatefulSet인 경우: 이름 그대로 사용
     */
    private String extractDeploymentName(V1Pod pod) {
        if (pod.getMetadata().getOwnerReferences() == null) return null;
        for (V1OwnerReference owner : pod.getMetadata().getOwnerReferences()) {
            if ("ReplicaSet".equals(owner.getKind())) {
                String rsName = owner.getName();
                int lastHyphen = rsName.lastIndexOf('-');
                return (lastHyphen > 0) ? rsName.substring(0, lastHyphen) : rsName;
            } else if ("Deployment".equals(owner.getKind()) || "DaemonSet".equals(owner.getKind()) || "StatefulSet".equals(owner.getKind())) {
                return owner.getName();
            }
        }
        return null;
    }

    /**
     * 객체를 JSON 문자열로 변환 (DB 저장용)
     * - IntOrString 타입 처리를 위한 커스텀 모듈 등록 필요
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON", e);
            return null;
        }
    }
}
