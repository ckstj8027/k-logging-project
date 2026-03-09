package com.k8s.cnapp.server.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.ingestion.dto.ClusterSnapshot;
import com.k8s.cnapp.server.profile.domain.*;
import com.k8s.cnapp.server.profile.repository.*;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            Tenant tenant = getCurrentTenant();
            if (tenant == null) {
                log.error("No authenticated tenant found. Skipping processing.");
                return;
            }

            ClusterSnapshot snapshot = objectMapper.readValue(rawData, ClusterSnapshot.class);
            logSummary(snapshot, tenant);

            processPods(snapshot.pods(), tenant);
            processServices(snapshot.services(), tenant);
            processNodes(snapshot.nodes(), tenant);
            processNamespaces(snapshot.namespaces(), tenant);
            processEvents(snapshot.events(), tenant);
            processDeployments(snapshot.deployments(), tenant);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse raw data to ClusterSnapshot", e);
        }
    }

    private Tenant getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Tenant) {
            return (Tenant) authentication.getPrincipal();
        }
        return null;
    }

    private void logSummary(ClusterSnapshot snapshot, Tenant tenant) {
        log.info("[Ingest] Received Snapshot for Tenant: {} (Pods: {}, Services: {}, Nodes: {}, Namespaces: {}, Events: {}, Deployments: {})",
                tenant.getName(),
                snapshot.pods() != null ? snapshot.pods().size() : 0,
                snapshot.services() != null ? snapshot.services().size() : 0,
                snapshot.nodes() != null ? snapshot.nodes().size() : 0,
                snapshot.namespaces() != null ? snapshot.namespaces().size() : 0,
                snapshot.events() != null ? snapshot.events().size() : 0,
                snapshot.deployments() != null ? snapshot.deployments().size() : 0
        );
    }

    // --- Fingerprint (Hash) Helper ---
    private String generateHash(Object... fields) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (Object field : fields) {
                sb.append(field == null ? "null" : field.toString()).append("|");
            }
            byte[] hashBytes = digest.digest(sb.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private void processPods(List<V1Pod> pods, Tenant tenant) {
        if (pods == null || pods.isEmpty()) return;

        List<String> keys = new ArrayList<>();
        for (V1Pod pod : pods) {
            if (pod.getMetadata() == null || pod.getSpec() == null) continue;
            if (pod.getSpec().getContainers() == null || pod.getSpec().getContainers().isEmpty()) continue;
            
            String namespace = pod.getMetadata().getNamespace();
            String podName = pod.getMetadata().getName();
            String containerName = pod.getSpec().getContainers().get(0).getName();
            String deploymentName = extractDeploymentName(pod);
            keys.add(namespace + "/" + (deploymentName != null ? deploymentName : podName) + "/" + containerName);
        }

        podProfileRepository.deleteByTenantAndKeysNotIn(tenant, keys);

        List<PodProfile> existingProfiles = podProfileRepository.findAllByTenantAndKeys(tenant, keys);
        Map<String, PodProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        p -> p.getAssetContext().getAssetKey() + "/" + p.getAssetContext().getContainerName(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<PodProfile> toSave = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();
        int updatedCount = 0;

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

            if (processedKeys.contains(key)) continue;

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

            // Fingerprint 계산 (보안 핵심 필드 중심)
            String newHash = generateHash(image, privileged, runAsUser, allowPrivilegeEscalation, readOnlyRootFilesystem);

            PodProfile existing = profileMap.get(key);
            if (existing != null) {
                String existingHash = generateHash(existing.getAssetContext().getImage(), existing.getPrivileged(), 
                                                   existing.getRunAsUser(), existing.getAllowPrivilegeEscalation(), 
                                                   existing.getReadOnlyRootFilesystem());
                
                // Fingerprint가 다를 때만 업데이트 (불필요한 Dirty Checking 방지)
                if (!newHash.equals(existingHash)) {
                    existing.updateAssetContext(new AssetContext(namespace, podName, containerName, image, deploymentName));
                    existing.updateSecurityContext(privileged, runAsUser, allowPrivilegeEscalation, readOnlyRootFilesystem);
                    updatedCount++;
                }
            } else {
                toSave.add(new PodProfile(
                        tenant,
                        new AssetContext(namespace, podName, containerName, image, deploymentName),
                        privileged, runAsUser, allowPrivilegeEscalation, readOnlyRootFilesystem
                ));
            }
            processedKeys.add(key);
        }
        
        if (!toSave.isEmpty()) podProfileRepository.saveAll(toSave);
        log.info("Pods - Total: {}, Inserted: {}, Updated: {}", pods.size(), toSave.size(), updatedCount);
    }

    private void processServices(List<V1Service> services, Tenant tenant) {
        if (services == null || services.isEmpty()) return;

        List<String> keys = new ArrayList<>();
        for (V1Service service : services) {
            if (service.getMetadata() == null) continue;
            keys.add(service.getMetadata().getNamespace() + "/" + service.getMetadata().getName());
        }

        serviceProfileRepository.deleteByTenantAndKeysNotIn(tenant, keys);

        List<ServiceProfile> existingProfiles = serviceProfileRepository.findAllByTenantAndKeysWithPorts(tenant, keys);
        Map<String, ServiceProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        s -> s.getNamespace() + "/" + s.getName(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        List<ServiceProfile> toSave = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();
        int updatedCount = 0;

        for (V1Service service : services) {
            if (service.getMetadata() == null || service.getSpec() == null) continue;
            String namespace = service.getMetadata().getNamespace();
            String name = service.getMetadata().getName();
            String key = namespace + "/" + name;

            if (processedKeys.contains(key)) continue;

            String type = service.getSpec().getType();
            String clusterIp = service.getSpec().getClusterIP();
            String externalIps = service.getSpec().getExternalIPs() != null ? String.join(",", service.getSpec().getExternalIPs()) : null;

            // 포트 정보를 문자열로 직렬화하여 Hash 생성에 포함
            String portsString = "";
            if (service.getSpec().getPorts() != null) {
                portsString = service.getSpec().getPorts().stream()
                        .map(p -> String.format("%s:%s:%s:%s", p.getProtocol(), p.getPort(), p.getTargetPort(), p.getNodePort()))
                        .sorted()
                        .collect(Collectors.joining(","));
            }

            String newHash = generateHash(type, clusterIp, externalIps, portsString);

            ServiceProfile existing = profileMap.get(key);
            if (existing != null) {
                String existingPortsString = existing.getPorts().stream()
                        .map(p -> String.format("%s:%s:%s:%s", p.getProtocol(), p.getPort(), p.getTargetPort(), p.getNodePort()))
                        .sorted()
                        .collect(Collectors.joining(","));
                String existingHash = generateHash(existing.getType(), existing.getClusterIp(), existing.getExternalIps(), existingPortsString);

                if (!newHash.equals(existingHash)) {
                    existing.update(type, clusterIp, externalIps);
                    
                    // Smart Collection Merge (전체 삭제 후 삽입 방지)
                    mergeServicePorts(existing, service.getSpec().getPorts());
                    updatedCount++;
                }
            } else {
                ServiceProfile newSp = new ServiceProfile(tenant, namespace, name, type, clusterIp, externalIps);
                if (service.getSpec().getPorts() != null) {
                    for (V1ServicePort port : service.getSpec().getPorts()) {
                        String targetPort = port.getTargetPort() != null ? port.getTargetPort().toString() : null;
                        newSp.addPort(new ServicePortProfile(port.getName(), port.getProtocol(), port.getPort(), targetPort, port.getNodePort()));
                    }
                }
                toSave.add(newSp);
            }
            processedKeys.add(key);
        }
        
        if (!toSave.isEmpty()) serviceProfileRepository.saveAll(toSave);
        log.info("Services - Total: {}, Inserted: {}, Updated: {}", services.size(), toSave.size(), updatedCount);
    }

    private void mergeServicePorts(ServiceProfile existing, List<V1ServicePort> newPorts) {
        if (newPorts == null || newPorts.isEmpty()) {
            existing.getPorts().clear();
            return;
        }

        List<ServicePortProfile> existingPorts = existing.getPorts();
        
        // 새로 들어온 포트 리스트를 기반으로 존재 여부 파악
        Set<String> newPortKeys = newPorts.stream()
                .map(p -> p.getProtocol() + ":" + p.getPort())
                .collect(Collectors.toSet());

        // 더 이상 존재하지 않는 포트 제거 (Smart Remove)
        existingPorts.removeIf(p -> !newPortKeys.contains(p.getProtocol() + ":" + p.getPort()));

        // 기존 포트를 Map으로 (빠른 매칭용)
        Map<String, ServicePortProfile> existingPortMap = existingPorts.stream()
                .collect(Collectors.toMap(
                        p -> p.getProtocol() + ":" + p.getPort(),
                        Function.identity()
                ));

        // 업데이트 또는 신규 추가
        for (V1ServicePort newPort : newPorts) {
            String portKey = newPort.getProtocol() + ":" + newPort.getPort();
            String targetPortStr = newPort.getTargetPort() != null ? newPort.getTargetPort().toString() : null;
            
            ServicePortProfile existingPort = existingPortMap.get(portKey);
            if (existingPort != null) {
                // 필요시 필드 갱신 로직 (여기서는 포트번호/프로토콜 외 타겟포트/노드포트 등 갱신 필요)
                // 만약 필드가 엔티티에 setter가 없다면 지우고 다시 넣거나 setter 추가 필요
                // 편의상 이 예제에서는 기존 포트가 있다면 그대로 유지 (일반적으로 변경되지 않음)
                // (더 정밀하게 하려면 ServicePortProfile에 update() 메서드 필요)
            } else {
                existing.addPort(new ServicePortProfile(
                        newPort.getName(), newPort.getProtocol(), newPort.getPort(), 
                        targetPortStr, newPort.getNodePort()
                ));
            }
        }
    }

    private void processNodes(List<V1Node> nodes, Tenant tenant) {
        if (nodes == null || nodes.isEmpty()) return;

        List<String> keys = nodes.stream()
                .filter(n -> n.getMetadata() != null)
                .map(n -> n.getMetadata().getName())
                .collect(Collectors.toList());

        nodeProfileRepository.deleteByTenantAndNameNotIn(tenant, keys);

        List<NodeProfile> existingProfiles = nodeProfileRepository.findByTenantAndNameIn(tenant, keys);
        Map<String, NodeProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        NodeProfile::getName,
                        Function.identity()
                ));

        List<NodeProfile> toSave = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();
        int updatedCount = 0;

        for (V1Node node : nodes) {
            if (node.getMetadata() == null || node.getStatus() == null) continue;
            String name = node.getMetadata().getName();
            
            if (processedKeys.contains(name)) continue;

            V1NodeSystemInfo nodeInfo = node.getStatus().getNodeInfo();
            String osImage = nodeInfo.getOsImage();
            String kernelVersion = nodeInfo.getKernelVersion();
            String containerRuntimeVersion = nodeInfo.getContainerRuntimeVersion();
            String kubeletVersion = nodeInfo.getKubeletVersion();
            
            String cpu = node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("cpu") != null 
                    ? node.getStatus().getCapacity().get("cpu").toSuffixedString() : "unknown";
            String memory = node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("memory") != null 
                    ? node.getStatus().getCapacity().get("memory").toSuffixedString() : "unknown";

            String newHash = generateHash(osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory);

            NodeProfile existing = profileMap.get(name);
            if (existing != null) {
                String existingHash = generateHash(existing.getOsImage(), existing.getKernelVersion(), 
                                                   existing.getContainerRuntimeVersion(), existing.getKubeletVersion(), 
                                                   existing.getCpuCapacity(), existing.getMemoryCapacity());
                
                if (!newHash.equals(existingHash)) {
                    existing.update(osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory);
                    updatedCount++;
                }
            } else {
                toSave.add(new NodeProfile(tenant, name, osImage, kernelVersion, containerRuntimeVersion, kubeletVersion, cpu, memory));
            }
            processedKeys.add(name);
        }
        
        if (!toSave.isEmpty()) nodeProfileRepository.saveAll(toSave);
        log.info("Nodes - Total: {}, Inserted: {}, Updated: {}", nodes.size(), toSave.size(), updatedCount);
    }

    private void processNamespaces(List<V1Namespace> namespaces, Tenant tenant) {
        if (namespaces == null || namespaces.isEmpty()) return;

        List<String> keys = namespaces.stream()
                .filter(n -> n.getMetadata() != null)
                .map(n -> n.getMetadata().getName())
                .collect(Collectors.toList());

        namespaceProfileRepository.deleteByTenantAndNameNotIn(tenant, keys);

        List<NamespaceProfile> existingProfiles = namespaceProfileRepository.findByTenantAndNameIn(tenant, keys);
        Map<String, NamespaceProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(NamespaceProfile::getName, Function.identity()));

        List<NamespaceProfile> toSave = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();
        int updatedCount = 0;

        for (V1Namespace ns : namespaces) {
            if (ns.getMetadata() == null || ns.getStatus() == null) continue;
            String name = ns.getMetadata().getName();
            
            if (processedKeys.contains(name)) continue;

            String status = ns.getStatus().getPhase();

            NamespaceProfile existing = profileMap.get(name);
            if (existing != null) {
                if (!Objects.equals(existing.getStatus(), status)) {
                    existing.update(status);
                    updatedCount++;
                }
            } else {
                toSave.add(new NamespaceProfile(tenant, name, status));
            }
            processedKeys.add(name);
        }
        
        if (!toSave.isEmpty()) namespaceProfileRepository.saveAll(toSave);
        log.info("Namespaces - Total: {}, Inserted: {}, Updated: {}", namespaces.size(), toSave.size(), updatedCount);
    }

    private void processEvents(List<CoreV1Event> events, Tenant tenant) {
        if (events == null || events.isEmpty()) return;

        List<String> keys = events.stream()
                .filter(e -> e.getMetadata() != null)
                .map(e -> e.getMetadata().getUid())
                .collect(Collectors.toList());

        eventProfileRepository.deleteByTenantAndUidNotIn(tenant, keys);

        List<EventProfile> existingProfiles = eventProfileRepository.findByTenantAndUidIn(tenant, keys);
        Map<String, EventProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(EventProfile::getUid, Function.identity()));

        List<EventProfile> toSave = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();
        int updatedCount = 0;

        for (CoreV1Event event : events) {
            if (event.getMetadata() == null || event.getInvolvedObject() == null) continue;
            String uid = event.getMetadata().getUid();
            
            if (processedKeys.contains(uid)) continue;

            Integer count = event.getCount();
            OffsetDateTime lastTimestamp = event.getLastTimestamp();
            String message = event.getMessage();

            String newHash = generateHash(count, lastTimestamp, message);

            EventProfile existing = profileMap.get(uid);
            if (existing != null) {
                String existingHash = generateHash(existing.getCount(), existing.getLastTimestamp(), existing.getMessage());
                if (!newHash.equals(existingHash)) {
                    existing.update(count, lastTimestamp, message);
                    updatedCount++;
                }
            } else {
                toSave.add(new EventProfile(
                        tenant, event.getMetadata().getNamespace(), event.getInvolvedObject().getKind(),
                        event.getInvolvedObject().getName(), event.getReason(), message,
                        event.getType(), lastTimestamp, count, uid
                ));
            }
            processedKeys.add(uid);
        }
        
        if (!toSave.isEmpty()) eventProfileRepository.saveAll(toSave);
        log.info("Events - Total: {}, Inserted: {}, Updated: {}", events.size(), toSave.size(), updatedCount);
    }
    
    private void processDeployments(List<V1Deployment> deployments, Tenant tenant) {
        if (deployments == null || deployments.isEmpty()) return;

        List<String> keys = new ArrayList<>();
        for (V1Deployment dep : deployments) {
            if (dep.getMetadata() == null) continue;
            keys.add(dep.getMetadata().getNamespace() + "/" + dep.getMetadata().getName());
        }

        deploymentProfileRepository.deleteByTenantAndKeysNotIn(tenant, keys);

        List<DeploymentProfile> existingProfiles = deploymentProfileRepository.findAllByTenantAndKeys(tenant, keys);
        Map<String, DeploymentProfile> profileMap = existingProfiles.stream()
                .collect(Collectors.toMap(
                        d -> d.getNamespace() + "/" + d.getName(),
                        Function.identity()
                ));

        List<DeploymentProfile> toSave = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();
        int updatedCount = 0;

        for (V1Deployment dep : deployments) {
            if (dep.getMetadata() == null || dep.getSpec() == null) continue;
            String namespace = dep.getMetadata().getNamespace();
            String name = dep.getMetadata().getName();
            String key = namespace + "/" + name;

            if (processedKeys.contains(key)) continue;

            Integer replicas = dep.getSpec().getReplicas();
            Integer availableReplicas = dep.getStatus() != null ? dep.getStatus().getAvailableReplicas() : 0;
            String strategy = dep.getSpec().getStrategy() != null ? dep.getSpec().getStrategy().getType() : null;
            String selectorJson = toJson(dep.getSpec().getSelector());

            String newHash = generateHash(replicas, availableReplicas, strategy, selectorJson);

            DeploymentProfile existing = profileMap.get(key);
            if (existing != null) {
                String existingHash = generateHash(existing.getReplicas(), existing.getAvailableReplicas(), 
                                                   existing.getStrategyType(), existing.getSelectorJson());
                if (!newHash.equals(existingHash)) {
                    existing.update(replicas, availableReplicas, strategy, selectorJson);
                    updatedCount++;
                }
            } else {
                toSave.add(new DeploymentProfile(tenant, namespace, name, replicas, availableReplicas, strategy, selectorJson));
            }
            processedKeys.add(key);
        }
        
        if (!toSave.isEmpty()) deploymentProfileRepository.saveAll(toSave);
        log.info("Deployments - Total: {}, Inserted: {}, Updated: {}", deployments.size(), toSave.size(), updatedCount);
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
