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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogProcessingService {

    private final ObjectMapper objectMapper;
    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final NamespaceProfileRepository namespaceProfileRepository;
    private final EventProfileRepository eventProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;

    @Transactional
    public void processRawData(String rawData) {
        try {
            ClusterSnapshot snapshot = objectMapper.readValue(rawData, ClusterSnapshot.class);
            logSummary(snapshot);

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

    private void processPods(List<V1Pod> pods) {
        if (pods == null || pods.isEmpty()) return;

        // 1. Bulk Fetch
        List<PodProfile> existingProfiles = podProfileRepository.findAll();
        Map<String, PodProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(p -> p.getAssetContext().getAssetKey() + "/" + p.getAssetContext().getContainerName(), Function.identity()));

        List<PodProfile> toSave = new ArrayList<>();

        for (V1Pod pod : pods) {
            if (pod.getMetadata() == null || pod.getSpec() == null) continue;
            String namespace = pod.getMetadata().getNamespace();
            String podName = pod.getMetadata().getName();
            
            if (pod.getSpec().getContainers() == null || pod.getSpec().getContainers().isEmpty()) continue;

            V1Container container = pod.getSpec().getContainers().get(0);
            String containerName = container.getName();
            String image = container.getImage();
            String deploymentName = extractDeploymentName(pod);
            String key = String.format("%s/%s/%s", namespace, deploymentName != null ? deploymentName : podName, containerName);

            // Security Context
            Boolean privileged = null;
            Long runAsUser = null;
            Boolean allowPrivilegeEscalation = null;
            Boolean readOnlyRootFilesystem = null;

            V1SecurityContext securityContext = container.getSecurityContext();
            if (securityContext != null) {
                privileged = securityContext.getPrivileged();
                runAsUser = securityContext.getRunAsUser();
                allowPrivilegeEscalation = securityContext.getAllowPrivilegeEscalation();
                readOnlyRootFilesystem = securityContext.getReadOnlyRootFilesystem();
            }
            if (runAsUser == null && pod.getSpec().getSecurityContext() != null) {
                runAsUser = pod.getSpec().getSecurityContext().getRunAsUser();
            }

            PodProfile existing = profileMap.get(key);
            if (existing != null) {
                // Update
                boolean changed = false;
                if (!Objects.equals(existing.getAssetContext().getImage(), image) || 
                    !Objects.equals(existing.getAssetContext().getDeploymentName(), deploymentName)) {
                    existing.updateAssetContext(new AssetContext(namespace, podName, containerName, image, deploymentName));
                    changed = true;
                }
                // Security Context Update (필요 시 메서드 추가하여 비교 로직 구현)
                // 여기서는 간단히 항상 업데이트 호출 (내부적으로 값 비교 가능)
                existing.updateSecurityContext(privileged, runAsUser, allowPrivilegeEscalation, readOnlyRootFilesystem);
                
                if (changed) toSave.add(existing); // 변경된 것만 저장 목록에 추가 (JPA라 사실 필요 없지만 명시적으로)
            } else {
                // Insert
                toSave.add(new PodProfile(
                        new AssetContext(namespace, podName, containerName, image, deploymentName),
                        Collections.emptyMap(),
                        privileged,
                        runAsUser,
                        allowPrivilegeEscalation,
                        readOnlyRootFilesystem
                ));
            }
        }
        
        if (!toSave.isEmpty()) {
            podProfileRepository.saveAll(toSave);
        }
        log.info("Processed Pods: Total={}, Saved/Updated={}", pods.size(), toSave.size());
    }

    private void processServices(List<V1Service> services) {
        if (services == null || services.isEmpty()) return;

        List<ServiceProfile> existingProfiles = serviceProfileRepository.findAllWithPorts();
        Map<String, ServiceProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(s -> s.getNamespace() + "/" + s.getName(), Function.identity()));

        List<ServiceProfile> toSave = new ArrayList<>();

        for (V1Service service : services) {
            if (service.getMetadata() == null || service.getSpec() == null) continue;
            String namespace = service.getMetadata().getNamespace();
            String name = service.getMetadata().getName();
            String key = namespace + "/" + name;

            String type = service.getSpec().getType();
            String clusterIp = service.getSpec().getClusterIP();
            String externalIps = service.getSpec().getExternalIPs() != null ? String.join(",", service.getSpec().getExternalIPs()) : null;

            ServiceProfile existing = profileMap.get(key);
            if (existing != null) {
                boolean isChanged = !Objects.equals(existing.getType(), type) ||
                                    !Objects.equals(existing.getClusterIp(), clusterIp) ||
                                    !Objects.equals(existing.getExternalIps(), externalIps);
                
                // 포트 비교 로직은 복잡하므로 일단 생략하고 무조건 갱신 (최적화 필요 시 추가)
                existing.clearPorts();
                if (service.getSpec().getPorts() != null) {
                    for (V1ServicePort port : service.getSpec().getPorts()) {
                        String targetPort = port.getTargetPort() != null ? port.getTargetPort().toString() : null;
                        existing.addPort(new ServicePortProfile(port.getName(), port.getProtocol(), port.getPort(), targetPort, port.getNodePort()));
                    }
                }
                
                if (isChanged) {
                    existing.update(type, clusterIp, externalIps);
                    toSave.add(existing);
                }
            } else {
                ServiceProfile newSp = new ServiceProfile(namespace, name, type, clusterIp, externalIps);
                if (service.getSpec().getPorts() != null) {
                    for (V1ServicePort port : service.getSpec().getPorts()) {
                        String targetPort = port.getTargetPort() != null ? port.getTargetPort().toString() : null;
                        newSp.addPort(new ServicePortProfile(port.getName(), port.getProtocol(), port.getPort(), targetPort, port.getNodePort()));
                    }
                }
                toSave.add(newSp);
            }
        }
        
        if (!toSave.isEmpty()) {
            serviceProfileRepository.saveAll(toSave);
        }
        log.info("Processed Services: Total={}, Saved/Updated={}", services.size(), toSave.size());
    }

    private void processNodes(List<V1Node> nodes) {
        if (nodes == null || nodes.isEmpty()) return;

        List<NodeProfile> existingProfiles = nodeProfileRepository.findAll();
        Map<String, NodeProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(NodeProfile::getName, Function.identity()));

        List<NodeProfile> toSave = new ArrayList<>();

        for (V1Node node : nodes) {
            if (node.getMetadata() == null || node.getStatus() == null) continue;
            String name = node.getMetadata().getName();
            V1NodeSystemInfo nodeInfo = node.getStatus().getNodeInfo();
            String osImage = nodeInfo.getOsImage();
            String kernelVersion = nodeInfo.getKernelVersion();
            String containerRuntimeVersion = nodeInfo.getContainerRuntimeVersion();
            String kubeletVersion = nodeInfo.getKubeletVersion();
            
            String cpu = node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("cpu") != null 
                    ? node.getStatus().getCapacity().get("cpu").toSuffixedString() : "unknown";
            String memory = node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("memory") != null 
                    ? node.getStatus().getCapacity().get("memory").toSuffixedString() : "unknown";

            NodeProfile existing = profileMap.get(name);
            if (existing != null) {
                boolean isChanged = !Objects.equals(existing.getOsImage(), osImage) ||
                                    !Objects.equals(existing.getKernelVersion(), kernelVersion) ||
                                    !Objects.equals(existing.getContainerRuntimeVersion(), containerRuntimeVersion) ||
                                    !Objects.equals(existing.getKubeletVersion(), kubeletVersion) ||
                                    !Objects.equals(existing.getCpuCapacity(), cpu) ||
                                    !Objects.equals(existing.getMemoryCapacity(), memory);

                if (isChanged) {
                    existing.update(osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory);
                    toSave.add(existing);
                }
            } else {
                toSave.add(new NodeProfile(name, osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory));
            }
        }
        
        if (!toSave.isEmpty()) {
            nodeProfileRepository.saveAll(toSave);
        }
        log.info("Processed Nodes: Total={}, Saved/Updated={}", nodes.size(), toSave.size());
    }

    private void processNamespaces(List<V1Namespace> namespaces) {
        if (namespaces == null || namespaces.isEmpty()) return;

        List<NamespaceProfile> existingProfiles = namespaceProfileRepository.findAll();
        Map<String, NamespaceProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(NamespaceProfile::getName, Function.identity()));

        List<NamespaceProfile> toSave = new ArrayList<>();

        for (V1Namespace ns : namespaces) {
            if (ns.getMetadata() == null || ns.getStatus() == null) continue;
            String name = ns.getMetadata().getName();
            String status = ns.getStatus().getPhase();

            NamespaceProfile existing = profileMap.get(name);
            if (existing != null) {
                if (!Objects.equals(existing.getStatus(), status)) {
                    existing.update(status);
                    toSave.add(existing);
                }
            } else {
                toSave.add(new NamespaceProfile(name, status));
            }
        }
        
        if (!toSave.isEmpty()) {
            namespaceProfileRepository.saveAll(toSave);
        }
        log.info("Processed Namespaces: Total={}, Saved/Updated={}", namespaces.size(), toSave.size());
    }

    private void processEvents(List<CoreV1Event> events) {
        if (events == null || events.isEmpty()) return;

        // 이벤트는 양이 많을 수 있으므로 UID 목록으로 조회하는 것이 나을 수 있으나,
        // 여기서는 일관성을 위해 전체 로드 방식을 유지하되, 실제 운영 시에는 최적화 필요 (예: 최근 1시간 데이터만 로드)
        List<EventProfile> existingProfiles = eventProfileRepository.findAll();
        Map<String, EventProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(EventProfile::getUid, Function.identity()));

        List<EventProfile> toSave = new ArrayList<>();

        for (CoreV1Event event : events) {
            if (event.getMetadata() == null || event.getInvolvedObject() == null) continue;
            String uid = event.getMetadata().getUid();
            Integer count = event.getCount();
            OffsetDateTime lastTimestamp = event.getLastTimestamp();
            String message = event.getMessage();

            EventProfile existing = profileMap.get(uid);
            if (existing != null) {
                if (!Objects.equals(existing.getCount(), count)) {
                    existing.update(count, lastTimestamp, message);
                    toSave.add(existing);
                }
            } else {
                toSave.add(new EventProfile(
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
            }
        }
        
        if (!toSave.isEmpty()) {
            eventProfileRepository.saveAll(toSave);
        }
        log.info("Processed Events: Total={}, Saved/Updated={}", events.size(), toSave.size());
    }
    
    private void processDeployments(List<V1Deployment> deployments) {
        if (deployments == null || deployments.isEmpty()) return;

        List<DeploymentProfile> existingProfiles = deploymentProfileRepository.findAll();
        Map<String, DeploymentProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(d -> d.getNamespace() + "/" + d.getName(), Function.identity()));

        List<DeploymentProfile> toSave = new ArrayList<>();

        for (V1Deployment dep : deployments) {
            if (dep.getMetadata() == null || dep.getSpec() == null) continue;
            String namespace = dep.getMetadata().getNamespace();
            String name = dep.getMetadata().getName();
            String key = namespace + "/" + name;

            Integer replicas = dep.getSpec().getReplicas();
            Integer availableReplicas = dep.getStatus() != null ? dep.getStatus().getAvailableReplicas() : 0;
            String strategy = dep.getSpec().getStrategy() != null ? dep.getSpec().getStrategy().getType() : null;
            String selectorJson = toJson(dep.getSpec().getSelector());

            DeploymentProfile existing = profileMap.get(key);
            if (existing != null) {
                boolean isChanged = !Objects.equals(existing.getReplicas(), replicas) ||
                                    !Objects.equals(existing.getAvailableReplicas(), availableReplicas) ||
                                    !Objects.equals(existing.getStrategyType(), strategy) ||
                                    !Objects.equals(existing.getSelectorJson(), selectorJson);

                if (isChanged) {
                    existing.update(replicas, availableReplicas, strategy, selectorJson);
                    toSave.add(existing);
                }
            } else {
                toSave.add(new DeploymentProfile(namespace, name, replicas, availableReplicas, strategy, selectorJson));
            }
        }
        
        if (!toSave.isEmpty()) {
            deploymentProfileRepository.saveAll(toSave);
        }
        log.info("Processed Deployments: Total={}, Saved/Updated={}", deployments.size(), toSave.size());
    }

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

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON", e);
            return null;
        }
    }
}
