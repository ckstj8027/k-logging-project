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

        // 1. Bulk Fetch & Map Mapping (Duplicate Key 해결)
        List<PodProfile> existingProfiles = podProfileRepository.findAll();
        Map<String, PodProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        p -> p.getAssetContext().getAssetKey() + "/" + p.getAssetContext().getContainerName(),
                        Function.identity(),
                        (existing, replacement) -> existing // 중복 키 발생 시 기존 값 유지
                ));

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
                // Update (Dirty Checking으로 자동 반영되지만, 명시적 처리를 위해 로직 유지)
                existing.updateAssetContext(new AssetContext(namespace, podName, containerName, image, deploymentName));
                existing.updateSecurityContext(privileged, runAsUser, allowPrivilegeEscalation, readOnlyRootFilesystem);
                // 기존 객체는 toSave에 넣지 않아도 @Transactional에 의해 업데이트됨
            } else {
                // Insert (신규 객체만 리스트에 추가)
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
        
        // 4. Batch Save (신규 객체만 저장)
        if (!toSave.isEmpty()) {
            podProfileRepository.saveAll(toSave);
        }
        log.info("Processed Pods: Total={}, New={}", pods.size(), toSave.size());
    }

    private void processServices(List<V1Service> services) {
        if (services == null || services.isEmpty()) return;

        List<ServiceProfile> existingProfiles = serviceProfileRepository.findAllWithPorts();
        Map<String, ServiceProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        s -> s.getNamespace() + "/" + s.getName(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

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
                existing.update(type, clusterIp, externalIps);
                
                existing.clearPorts();
                if (service.getSpec().getPorts() != null) {
                    for (V1ServicePort port : service.getSpec().getPorts()) {
                        String targetPort = port.getTargetPort() != null ? port.getTargetPort().toString() : null;
                        existing.addPort(new ServicePortProfile(port.getName(), port.getProtocol(), port.getPort(), targetPort, port.getNodePort()));
                    }
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
        log.info("Processed Services: Total={}, New={}", services.size(), toSave.size());
    }

    private void processNodes(List<V1Node> nodes) {
        if (nodes == null || nodes.isEmpty()) return;

        List<NodeProfile> existingProfiles = nodeProfileRepository.findAll();
        Map<String, NodeProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        NodeProfile::getName,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

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
                existing.update(osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory);
            } else {
                toSave.add(new NodeProfile(name, osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory));
            }
        }
        
        if (!toSave.isEmpty()) {
            nodeProfileRepository.saveAll(toSave);
        }
        log.info("Processed Nodes: Total={}, New={}", nodes.size(), toSave.size());
    }

    private void processNamespaces(List<V1Namespace> namespaces) {
        if (namespaces == null || namespaces.isEmpty()) return;

        List<NamespaceProfile> existingProfiles = namespaceProfileRepository.findAll();
        Map<String, NamespaceProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        NamespaceProfile::getName,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<NamespaceProfile> toSave = new ArrayList<>();

        for (V1Namespace ns : namespaces) {
            if (ns.getMetadata() == null || ns.getStatus() == null) continue;
            String name = ns.getMetadata().getName();
            String status = ns.getStatus().getPhase();

            NamespaceProfile existing = profileMap.get(name);
            if (existing != null) {
                existing.update(status);
            } else {
                toSave.add(new NamespaceProfile(name, status));
            }
        }
        
        if (!toSave.isEmpty()) {
            namespaceProfileRepository.saveAll(toSave);
        }
        log.info("Processed Namespaces: Total={}, New={}", namespaces.size(), toSave.size());
    }

    private void processEvents(List<CoreV1Event> events) {
        if (events == null || events.isEmpty()) return;

        List<EventProfile> existingProfiles = eventProfileRepository.findAll();
        Map<String, EventProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        EventProfile::getUid,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<EventProfile> toSave = new ArrayList<>();

        for (CoreV1Event event : events) {
            if (event.getMetadata() == null || event.getInvolvedObject() == null) continue;
            String uid = event.getMetadata().getUid();
            Integer count = event.getCount();
            OffsetDateTime lastTimestamp = event.getLastTimestamp();
            String message = event.getMessage();

            EventProfile existing = profileMap.get(uid);
            if (existing != null) {
                existing.update(count, lastTimestamp, message);
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
        log.info("Processed Events: Total={}, New={}", events.size(), toSave.size());
    }
    
    private void processDeployments(List<V1Deployment> deployments) {
        if (deployments == null || deployments.isEmpty()) return;

        List<DeploymentProfile> existingProfiles = deploymentProfileRepository.findAll();
        Map<String, DeploymentProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        d -> d.getNamespace() + "/" + d.getName(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

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
                existing.update(replicas, availableReplicas, strategy, selectorJson);
            } else {
                toSave.add(new DeploymentProfile(namespace, name, replicas, availableReplicas, strategy, selectorJson));
            }
        }
        
        if (!toSave.isEmpty()) {
            deploymentProfileRepository.saveAll(toSave);
        }
        log.info("Processed Deployments: Total={}, New={}", deployments.size(), toSave.size());
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
