package com.k8s.cnapp.server.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.detection.event.ScanRequestEvent;
import com.k8s.cnapp.server.detection.event.SecurityEventPublisher;
import com.k8s.cnapp.server.ingestion.dto.ClusterSnapshot;
import com.k8s.cnapp.server.policy.domain.Policy;
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
import java.time.LocalDateTime;
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
    private final SecurityEventPublisher securityEventPublisher; // MQ 퍼블리셔로 교체

    @Transactional
    public void processRawData(Tenant tenant, String rawData) {
        try {
            if (tenant == null) return;

            ClusterSnapshot snapshot = objectMapper.readValue(rawData, ClusterSnapshot.class);
            Map<Policy.ResourceType, List<Long>> updatedMap = new EnumMap<>(Policy.ResourceType.class);

            // 1. 삭제된 리소스 처리
            processDeletions(snapshot.deletedResources(), tenant);

            // 2. 6대 핵심 자산 처리 및 변경 ID 수집
            updatedMap.put(Policy.ResourceType.POD, processPods(snapshot.pods(), tenant));
            updatedMap.put(Policy.ResourceType.SERVICE, processServices(snapshot.services(), tenant));
            updatedMap.put(Policy.ResourceType.NODE, processNodes(snapshot.nodes(), tenant));
            updatedMap.put(Policy.ResourceType.DEPLOYMENT, processDeployments(snapshot.deployments(), tenant));
            updatedMap.put(Policy.ResourceType.NAMESPACE, processNamespaces(snapshot.namespaces(), tenant));
            updatedMap.put(Policy.ResourceType.EVENT, processEvents(snapshot.events(), tenant));

            boolean hasChanges = updatedMap.entrySet().stream()
                    .filter(e -> e.getKey() != Policy.ResourceType.EVENT) // NAMESPACE 제외 로직 제거
                    .anyMatch(e -> !e.getValue().isEmpty());

            if (hasChanges) {
                log.info("Significant changes detected for tenant: {}. Publishing MQ scan request.", tenant.getName());
                securityEventPublisher.publishScanRequest(ScanRequestEvent.builder()
                        .tenantId(tenant.getId())
                        .updatedResourceIds(updatedMap)
                        .targetedScan(true)
                        .build());
            }
        } catch (Exception e) {
            log.error("Log Ingestion failed", e);
        }
    }

    private void processDeletions(Map<String, List<String>> deletedResources, Tenant tenant) {
        if (deletedResources == null || deletedResources.isEmpty()) return;

        log.info("Processing explicit deletions for tenant: {}. Resource types to check: {}", tenant.getName(), deletedResources.keySet());

        for (Map.Entry<String, List<String>> entry : deletedResources.entrySet()) {
            String type = entry.getKey();
            List<String> keys = entry.getValue();

            if (keys == null || keys.isEmpty()) continue;

            log.debug("Checking deletion for {} entries of type {} with keys: {}", keys.size(), type, keys);

            try {
                switch (type.toUpperCase()) {
                    case "POD":
                        List<PodProfile> pods = podProfileRepository.findAllByTenantAndKeys(tenant, keys);
                        if (!pods.isEmpty()) {
                            log.info("Found {} Pods to delete for tenant {}", pods.size(), tenant.getName());
                            podProfileRepository.deleteAllInBatch(pods);
                        } else {
                            log.debug("No Pods found matching keys: {}", keys);
                        }
                        break;
                    case "SERVICE":
                        List<ServiceProfile> services = serviceProfileRepository.findAllByTenantAndKeysWithPorts(tenant, keys);
                        if (!services.isEmpty()) {
                            log.info("Found {} Services to delete for tenant {}", services.size(), tenant.getName());
                            serviceProfileRepository.deleteAllInBatch(services);
                        }
                        break;
                    case "NODE":
                        List<NodeProfile> nodes = nodeProfileRepository.findByTenantAndNameIn(tenant, keys);
                        if (!nodes.isEmpty()) {
                            log.info("Found {} Nodes to delete for tenant {}", nodes.size(), tenant.getName());
                            nodeProfileRepository.deleteAllInBatch(nodes);
                        }
                        break;
                    case "DEPLOYMENT":
                        List<DeploymentProfile> deployments = deploymentProfileRepository.findAllByTenantAndKeys(tenant, keys);
                        if (!deployments.isEmpty()) {
                            log.info("Found {} Deployments to delete for tenant {}", deployments.size(), tenant.getName());
                            deploymentProfileRepository.deleteAllInBatch(deployments);
                        }
                        break;
                    case "NAMESPACE":
                        List<NamespaceProfile> namespaces = namespaceProfileRepository.findByTenantAndNameIn(tenant, keys);
                        if (!namespaces.isEmpty()) {
                            log.info("Found {} Namespaces to delete for tenant {}", namespaces.size(), tenant.getName());
                            namespaceProfileRepository.deleteAllInBatch(namespaces);
                        }
                        break;
                    case "EVENT":
                        List<EventProfile> events = eventProfileRepository.findByTenantAndUidIn(tenant, keys);
                        if (!events.isEmpty()) {
                            log.info("Found {} Events to delete for tenant {}", events.size(), tenant.getName());
                            eventProfileRepository.deleteAllInBatch(events);
                        }
                        break;
                    default:
                        log.warn("Resource type '{}' is not explicitly handled for deletion or has no storage mapping.", type);
                }
            } catch (Exception e) {
                log.error("Error during deletion process for type {}: {}", type, e.getMessage(), e);
            }
        }
    }

    private List<Long> processPods(List<V1Pod> pods, Tenant tenant) {
        if (pods == null || pods.isEmpty()) return Collections.emptyList();
        List<Long> changedIds = new ArrayList<>();
        List<String> keys = pods.stream()
                .filter(p -> p.getMetadata() != null && p.getSpec() != null && !p.getSpec().getContainers().isEmpty())
                .map(p -> p.getMetadata().getNamespace() + "/" + p.getMetadata().getName() + "/" + p.getSpec().getContainers().get(0).getName())
                .collect(Collectors.toList());

        Map<String, PodProfile> profileMap = podProfileRepository.findAllByTenantAndKeys(tenant, keys).stream()
                .collect(Collectors.toMap(p -> p.getAssetContext().getLookupKey(), Function.identity()));

        LocalDateTime now = LocalDateTime.now();
        List<PodProfile> toSave = new ArrayList<>();

        for (V1Pod pod : pods) {
            if (pod.getMetadata() == null || pod.getSpec() == null || pod.getSpec().getContainers().isEmpty()) continue;
            V1Container container = pod.getSpec().getContainers().get(0);
            String key = pod.getMetadata().getNamespace() + "/" + pod.getMetadata().getName() + "/" + container.getName();
            
            V1SecurityContext sc = container.getSecurityContext();
            Boolean privileged = sc != null ? sc.getPrivileged() : null;
            Long runAsUser = sc != null ? sc.getRunAsUser() : null;
            Boolean allowPrivEsc = sc != null ? sc.getAllowPrivilegeEscalation() : null;
            Boolean roRootFs = sc != null ? sc.getReadOnlyRootFilesystem() : null;
            if (runAsUser == null && pod.getSpec().getSecurityContext() != null) runAsUser = pod.getSpec().getSecurityContext().getRunAsUser();

            // 실시간 상태 데이터 추출
            String status = pod.getStatus() != null ? pod.getStatus().getPhase() : "Unknown";
            String podIp = pod.getStatus() != null ? pod.getStatus().getPodIP() : null;
            String nodeName = pod.getSpec().getNodeName();

            String newHash = generateHash(container.getImage(), privileged, runAsUser, allowPrivEsc, roRootFs, status, podIp, nodeName);
            PodProfile existing = profileMap.get(key);

            if (existing != null) {
                String existingHash = generateHash(existing.getAssetContext().getImage(), existing.getPrivileged(), existing.getRunAsUser(), existing.getAllowPrivilegeEscalation(), existing.getReadOnlyRootFilesystem(), existing.getAssetContext().getStatus(), existing.getAssetContext().getPodIp(), existing.getAssetContext().getNodeName());
                boolean dataChanged = !newHash.equals(existingHash);
                if (dataChanged || existing.getLastSeenAt() == null || existing.getLastSeenAt().isBefore(now.minusMinutes(1))) {
                    existing.updateLastSeenAt(now);
                    if (dataChanged) {
                        existing.updateAssetContext(new AssetContext(pod.getMetadata().getNamespace(), pod.getMetadata().getName(), container.getName(), container.getImage(), extractDeploymentName(pod), status, podIp, nodeName));
                        existing.updateSecurityContext(privileged, runAsUser, allowPrivEsc, roRootFs);
                        changedIds.add(existing.getId());
                    }
                }
            } else {
                PodProfile newPod = new PodProfile(tenant, new AssetContext(pod.getMetadata().getNamespace(), pod.getMetadata().getName(), container.getName(), container.getImage(), extractDeploymentName(pod), status, podIp, nodeName), privileged, runAsUser, allowPrivEsc, roRootFs, "0m", "0Mi");
                newPod.updateLastSeenAt(now);
                toSave.add(newPod);
            }
        }
        if (!toSave.isEmpty()) podProfileRepository.saveAll(toSave).forEach(p -> changedIds.add(p.getId()));
        return changedIds;
    }

    private List<Long> processServices(List<V1Service> services, Tenant tenant) {
        if (services == null || services.isEmpty()) return Collections.emptyList();
        List<Long> changedIds = new ArrayList<>();
        List<String> keys = services.stream().filter(s -> s.getMetadata() != null).map(s -> s.getMetadata().getNamespace() + "/" + s.getMetadata().getName()).toList();
        Map<String, ServiceProfile> profileMap = serviceProfileRepository.findAllByTenantAndKeysWithPorts(tenant, keys).stream().collect(Collectors.toMap(s -> s.getNamespace() + "/" + s.getName(), Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        List<ServiceProfile> toSave = new ArrayList<>();

        for (V1Service service : services) {
            if (service.getMetadata() == null || service.getSpec() == null) continue;
            String key = service.getMetadata().getNamespace() + "/" + service.getMetadata().getName();
            String type = service.getSpec().getType();
            String cIp = service.getSpec().getClusterIP();
            String eIps = service.getSpec().getExternalIPs() != null ? String.join(",", service.getSpec().getExternalIPs()) : null;
            String portsStr = service.getSpec().getPorts() != null ? service.getSpec().getPorts().stream().map(p -> p.getProtocol() + ":" + p.getPort()).sorted().collect(Collectors.joining(",")) : "";

            String newHash = generateHash(type, cIp, eIps, portsStr);
            ServiceProfile existing = profileMap.get(key);

            if (existing != null) {
                String existingPorts = existing.getPorts().stream().map(p -> p.getProtocol() + ":" + p.getPort()).sorted().collect(Collectors.joining(","));
                String existingHash = generateHash(existing.getType(), existing.getClusterIp(), existing.getExternalIps(), existingPorts);
                boolean dataChanged = !newHash.equals(existingHash);
                if (dataChanged || existing.getLastSeenAt() == null || existing.getLastSeenAt().isBefore(now.minusMinutes(1))) {
                    existing.updateLastSeenAt(now);
                    if (dataChanged) {
                        existing.update(type, cIp, eIps);
                        mergeServicePorts(existing, service.getSpec().getPorts());
                        changedIds.add(existing.getId());
                    }
                }
            } else {
                ServiceProfile ns = new ServiceProfile(tenant, service.getMetadata().getNamespace(), service.getMetadata().getName(), type, cIp, eIps);
                if (service.getSpec().getPorts() != null) service.getSpec().getPorts().forEach(p -> ns.addPort(new ServicePortProfile(p.getName(), p.getProtocol(), p.getPort(), p.getTargetPort() != null ? p.getTargetPort().toString() : null, p.getNodePort())));
                ns.updateLastSeenAt(now);
                toSave.add(ns);
            }
        }
        if (!toSave.isEmpty()) serviceProfileRepository.saveAll(toSave).forEach(s -> changedIds.add(s.getId()));
        return changedIds;
    }

    private List<Long> processNodes(List<V1Node> nodes, Tenant tenant) {
        if (nodes == null || nodes.isEmpty()) return Collections.emptyList();
        List<Long> changedIds = new ArrayList<>();
        List<String> keys = nodes.stream().filter(n -> n.getMetadata() != null).map(n -> n.getMetadata().getName()).toList();
        Map<String, NodeProfile> profileMap = nodeProfileRepository.findByTenantAndNameIn(tenant, keys).stream().collect(Collectors.toMap(NodeProfile::getName, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        List<NodeProfile> toSave = new ArrayList<>();

        for (V1Node node : nodes) {
            if (node.getMetadata() == null || node.getStatus() == null) continue;
            V1NodeSystemInfo info = node.getStatus().getNodeInfo();
            String cpu = node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("cpu") != null ? node.getStatus().getCapacity().get("cpu").toSuffixedString() : "unknown";
            String mem = node.getStatus().getCapacity() != null && node.getStatus().getCapacity().get("memory") != null ? node.getStatus().getCapacity().get("memory").toSuffixedString() : "unknown";
            String newHash = generateHash(info.getOsImage(), info.getKernelVersion(), info.getKubeletVersion(), cpu, mem);
            NodeProfile existing = profileMap.get(node.getMetadata().getName());

            if (existing != null) {
                String existingHash = generateHash(existing.getOsImage(), existing.getKernelVersion(), existing.getKubeletVersion(), existing.getCpuCapacity(), existing.getMemoryCapacity());
                boolean changed = !newHash.equals(existingHash);
                if (changed || existing.getLastSeenAt() == null || existing.getLastSeenAt().isBefore(now.minusMinutes(1))) {
                    existing.updateLastSeenAt(now);
                    if (changed) {
                        existing.update(info.getOsImage(), info.getKernelVersion(), info.getContainerRuntimeVersion(), info.getKubeletVersion(), cpu, mem);
                        changedIds.add(existing.getId());
                    }
                }
            } else {
                NodeProfile nn = new NodeProfile(tenant, node.getMetadata().getName(), info.getOsImage(), info.getKernelVersion(), info.getContainerRuntimeVersion(), info.getKubeletVersion(), cpu, mem);
                nn.updateLastSeenAt(now);
                toSave.add(nn);
            }
        }
        if (!toSave.isEmpty()) nodeProfileRepository.saveAll(toSave).forEach(n -> changedIds.add(n.getId()));
        return changedIds;
    }

    private List<Long> processNamespaces(List<V1Namespace> namespaces, Tenant tenant) {
        if (namespaces == null || namespaces.isEmpty()) return Collections.emptyList();
        List<Long> changedIds = new ArrayList<>();
        List<String> names = namespaces.stream().filter(n -> n.getMetadata() != null).map(n -> n.getMetadata().getName()).toList();
        Map<String, NamespaceProfile> profileMap = namespaceProfileRepository.findByTenantAndNameIn(tenant, names).stream().collect(Collectors.toMap(NamespaceProfile::getName, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        List<NamespaceProfile> toSave = new ArrayList<>();

        for (V1Namespace ns : namespaces) {
            if (ns.getMetadata() == null || ns.getStatus() == null) continue;
            String status = ns.getStatus().getPhase();
            NamespaceProfile existing = profileMap.get(ns.getMetadata().getName());
            if (existing != null) {
                boolean changed = !Objects.equals(existing.getStatus(), status);
                if (changed || existing.getLastSeenAt() == null || existing.getLastSeenAt().isBefore(now.minusMinutes(1))) {
                    existing.updateLastSeenAt(now);
                    if (changed) { existing.update(status); changedIds.add(existing.getId()); }
                }
            } else {
                NamespaceProfile nns = new NamespaceProfile(tenant, ns.getMetadata().getName(), status);
                nns.updateLastSeenAt(now);
                toSave.add(nns);
            }
        }
        if (!toSave.isEmpty()) namespaceProfileRepository.saveAll(toSave).forEach(n -> changedIds.add(n.getId()));
        return changedIds;
    }

    private List<Long> processEvents(List<CoreV1Event> events, Tenant tenant) {
        if (events == null || events.isEmpty()) return Collections.emptyList();
        List<Long> changedIds = new ArrayList<>();
        List<String> uids = events.stream().filter(e -> e.getMetadata() != null && e.getMetadata().getUid() != null).map(e -> e.getMetadata().getUid()).toList();
        Map<String, EventProfile> profileMap = eventProfileRepository.findByTenantAndUidIn(tenant, uids).stream().collect(Collectors.toMap(EventProfile::getUid, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        List<EventProfile> toSave = new ArrayList<>();

        for (CoreV1Event event : events) {
            if (event.getMetadata() == null || event.getMetadata().getUid() == null || event.getInvolvedObject() == null) continue;
            String uid = event.getMetadata().getUid();
            String msg = event.getMessage();
            String newHash = generateHash(event.getCount(), msg);
            EventProfile existing = profileMap.get(uid);

            if (existing != null) {
                String existingHash = generateHash(existing.getCount(), existing.getMessage());
                boolean changed = !newHash.equals(existingHash);
                if (changed || existing.getLastSeenAt() == null || existing.getLastSeenAt().isBefore(now.minusMinutes(1))) {
                    existing.updateLastSeenAt(now);
                    if (changed) { existing.update(event.getCount(), event.getLastTimestamp(), msg); changedIds.add(existing.getId()); }
                }
            } else {
                EventProfile ne = new EventProfile(tenant, event.getInvolvedObject().getNamespace(), event.getInvolvedObject().getKind(), event.getInvolvedObject().getName(), event.getReason(), msg, event.getType(), event.getLastTimestamp(), event.getCount(), uid);
                ne.updateLastSeenAt(now);
                toSave.add(ne);
            }
        }
        if (!toSave.isEmpty()) eventProfileRepository.saveAll(toSave).forEach(e -> changedIds.add(e.getId()));
        return changedIds;
    }

    private List<Long> processDeployments(List<V1Deployment> deployments, Tenant tenant) {
        if (deployments == null || deployments.isEmpty()) return Collections.emptyList();
        List<Long> changedIds = new ArrayList<>();
        List<String> keys = deployments.stream().filter(d -> d.getMetadata() != null).map(d -> d.getMetadata().getNamespace() + "/" + d.getMetadata().getName()).toList();
        Map<String, DeploymentProfile> profileMap = deploymentProfileRepository.findAllByTenantAndKeys(tenant, keys).stream().collect(Collectors.toMap(d -> d.getNamespace() + "/" + d.getName(), Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        List<DeploymentProfile> toSave = new ArrayList<>();

        for (V1Deployment dep : deployments) {
            if (dep.getMetadata() == null || dep.getSpec() == null) continue;
            Integer reps = dep.getSpec().getReplicas();
            String strategy = dep.getSpec().getStrategy() != null ? dep.getSpec().getStrategy().getType() : null;
            String newHash = generateHash(reps, strategy);
            DeploymentProfile existing = profileMap.get(dep.getMetadata().getNamespace() + "/" + dep.getMetadata().getName());

            if (existing != null) {
                String existingHash = generateHash(existing.getReplicas(), existing.getStrategyType());
                boolean changed = !newHash.equals(existingHash);
                if (changed || existing.getLastSeenAt() == null || existing.getLastSeenAt().isBefore(now.minusMinutes(1))) {
                    existing.updateLastSeenAt(now);
                    if (changed) {
                        existing.update(reps, dep.getStatus() != null ? dep.getStatus().getAvailableReplicas() : 0, strategy, toJson(dep.getSpec().getSelector()));
                        changedIds.add(existing.getId());
                    }
                }
            } else {
                DeploymentProfile nd = new DeploymentProfile(tenant, dep.getMetadata().getNamespace(), dep.getMetadata().getName(), reps, dep.getStatus() != null ? dep.getStatus().getAvailableReplicas() : 0, strategy, toJson(dep.getSpec().getSelector()));
                nd.updateLastSeenAt(now);
                toSave.add(nd);
            }
        }
        if (!toSave.isEmpty()) deploymentProfileRepository.saveAll(toSave).forEach(d -> changedIds.add(d.getId()));
        return changedIds;
    }

    private void mergeServicePorts(ServiceProfile existing, List<V1ServicePort> newPorts) {
        if (newPorts == null || newPorts.isEmpty()) { existing.getPorts().clear(); return; }
        Set<String> newKeys = newPorts.stream().map(p -> p.getProtocol() + ":" + p.getPort()).collect(Collectors.toSet());
        existing.getPorts().removeIf(p -> !newKeys.contains(p.getProtocol() + ":" + p.getPort()));
        Map<String, ServicePortProfile> existingMap = existing.getPorts().stream().collect(Collectors.toMap(p -> p.getProtocol() + ":" + p.getPort(), Function.identity()));
        for (V1ServicePort np : newPorts) {
            if (!existingMap.containsKey(np.getProtocol() + ":" + np.getPort()))
                existing.addPort(new ServicePortProfile(np.getName(), np.getProtocol(), np.getPort(), np.getTargetPort() != null ? np.getTargetPort().toString() : null, np.getNodePort()));
        }
    }

    private Tenant getCurrentTenant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof Tenant) ? (Tenant) auth.getPrincipal() : null;
    }

    private String generateHash(Object... fields) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (Object f : fields) sb.append(f == null ? "null" : f.toString()).append("|");
            byte[] hashBytes = digest.digest(sb.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) { String hex = Integer.toHexString(0xff & b); if (hex.length() == 1) hexString.append('0'); hexString.append(hex); }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
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
        try { return objectMapper.writeValueAsString(obj); } catch (Exception e) { return null; }
    }
}
