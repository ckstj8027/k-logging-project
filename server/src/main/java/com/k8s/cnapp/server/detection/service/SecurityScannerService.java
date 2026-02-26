package com.k8s.cnapp.server.detection.service;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.profile.domain.*;
import com.k8s.cnapp.server.profile.repository.DeploymentProfileRepository;
import com.k8s.cnapp.server.profile.repository.NodeProfileRepository;
import com.k8s.cnapp.server.profile.repository.PodProfileRepository;
import com.k8s.cnapp.server.profile.repository.ServiceProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityScannerService {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final NodeProfileRepository nodeProfileRepository;
    private final AlertRepository alertRepository;

    // 위험 포트 목록 (SSH, Telnet, SMTP, DNS, POP3, IMAP, LDAP, SMB, RDP, DB 등)
    private static final Set<Integer> DANGEROUS_PORTS = Set.of(
            21, 22, 23, 25, 53, 110, 143, 389, 445, 3306, 3389, 5432, 6379, 27017
    );

    // 노드 리소스 임계값 (예시: CPU 1코어, Memory 1GB)
    // 실제 운영 환경에서는 설정 파일로 관리하거나 동적으로 조정 필요
    private static final int CPU_LIMIT_CORES = 1;
    private static final long MEMORY_LIMIT_BYTES = 1L * 1024 * 1024 * 1024; // 32GB

    // 1분마다 스캔 실행
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scan() {
        log.info("Starting security scan...");
        try {
            // 1. 기존 OPEN 상태 Alert 벌크 조회 (메모리 로딩)
            List<Alert> openAlerts = alertRepository.findByStatus(Alert.Status.OPEN);
            Map<String, Alert> alertMap = openAlerts.stream()
                    .collect(Collectors.toMap(
                            a -> a.getResourceType() + "/" + a.getResourceName() + "/" + a.getMessage(),
                            Function.identity(),
                            (existing, replacement) -> existing // 중복 시 기존 것 유지
                    ));

            List<Alert> newAlerts = new ArrayList<>();

            // 2. 각 리소스 스캔 (메모리 맵을 통해 중복 체크)
            scanPods(alertMap, newAlerts);
            scanServices(alertMap, newAlerts);
            scanDeployments(alertMap, newAlerts);
            scanNodes(alertMap, newAlerts);

            // 3. 신규 Alert 일괄 저장
            if (!newAlerts.isEmpty()) {
                alertRepository.saveAll(newAlerts);
                log.info("Created {} new alerts.", newAlerts.size());
            } else {
                log.info("No new alerts created.");
            }

            log.info("Security scan completed.");
        } catch (Exception e) {
            log.error("Error during security scan", e);
        }
    }

    private void scanPods(Map<String, Alert> alertMap, List<Alert> newAlerts) {
        List<PodProfile> pods = podProfileRepository.findAll();
        log.info("Scanning {} pods...", pods.size());
        for (PodProfile pod : pods) {
            String resourceName = pod.getAssetContext().getAssetKey();

            if (Boolean.TRUE.equals(pod.getPrivileged())) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.CRITICAL, Alert.Category.CSPM,
                        "Privileged container detected", "Pod", resourceName);
            }

            if (Boolean.TRUE.equals(pod.getRunAsRoot())) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.HIGH, Alert.Category.CSPM,
                        "Container running as root", "Pod", resourceName);
            }

            if ("default".equals(pod.getAssetContext().getNamespace())) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.LOW, Alert.Category.CSPM,
                        "Pod running in default namespace", "Pod", resourceName);
            }
            
            if (pod.getAssetContext().getImage() != null && pod.getAssetContext().getImage().endsWith(":latest")) {
                 addAlertIfNew(alertMap, newAlerts, Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Image using 'latest' tag", "Pod", resourceName);
            }
        }
    }

    private void scanServices(Map<String, Alert> alertMap, List<Alert> newAlerts) {
        List<ServiceProfile> services = serviceProfileRepository.findAllWithPorts();
        log.info("Scanning {} services...", services.size());
        for (ServiceProfile service : services) {
            String resourceName = service.getNamespace() + "/" + service.getName();

            if ("LoadBalancer".equals(service.getType()) || "NodePort".equals(service.getType())) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Service exposed externally (" + service.getType() + ")", "Service", resourceName);
                
                for (ServicePortProfile port : service.getPorts()) {
                    if (port.getPort() != null && DANGEROUS_PORTS.contains(port.getPort())) {
                        addAlertIfNew(alertMap, newAlerts, Alert.Severity.HIGH, Alert.Category.CSPM,
                                "Dangerous port exposed: " + port.getPort() + " (" + port.getProtocol() + ")", 
                                "Service", resourceName);
                    }
                }
            }
        }
    }

    private void scanDeployments(Map<String, Alert> alertMap, List<Alert> newAlerts) {
        List<DeploymentProfile> deployments = deploymentProfileRepository.findAll();
        log.info("Scanning {} deployments...", deployments.size());
        for (DeploymentProfile deployment : deployments) {
            String resourceName = deployment.getNamespace() + "/" + deployment.getName();

            if (deployment.getReplicas() != null && deployment.getReplicas() >= 3) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.LOW, Alert.Category.CSPM,
                        "High replica count detected (" + deployment.getReplicas() + ")", "Deployment", resourceName);
            }
            
            if (deployment.getReplicas() != null && deployment.getReplicas() == 0) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Zero replicas detected (Service might be down)", "Deployment", resourceName);
            }
        }
    }

    private void scanNodes(Map<String, Alert> alertMap, List<Alert> newAlerts) {
        List<NodeProfile> nodes = nodeProfileRepository.findAll();
        log.info("Scanning {} nodes...", nodes.size());
        for (NodeProfile node : nodes) {
            String resourceName = node.getName();

            // CPU Capacity Check
            long cpuCores = parseCpu(node.getCpuCapacity());
            if (cpuCores > CPU_LIMIT_CORES) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.HIGH, Alert.Category.CSPM,
                        "Node CPU capacity exceeds limit (" + cpuCores + " > " + CPU_LIMIT_CORES + ")", "Node", resourceName);
            }

            // Memory Capacity Check
            long memoryBytes = parseMemory(node.getMemoryCapacity());
            if (memoryBytes > MEMORY_LIMIT_BYTES) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.HIGH, Alert.Category.CSPM,
                        "Node Memory capacity exceeds limit (" + (memoryBytes / 1024 / 1024 / 1024) + "GB > " + (MEMORY_LIMIT_BYTES / 1024 / 1024 / 1024) + "GB)", "Node", resourceName);
            }
        }
    }

    private long parseCpu(String cpuStr) {
        if (cpuStr == null) return 0;
        try {
            if (cpuStr.endsWith("m")) {
                return Long.parseLong(cpuStr.substring(0, cpuStr.length() - 1)) / 1000;
            }
            return Long.parseLong(cpuStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseMemory(String memStr) {
        if (memStr == null) return 0;
        try {
            String lower = memStr.toLowerCase();
            if (lower.endsWith("ki")) return Long.parseLong(memStr.substring(0, memStr.length() - 2)) * 1024;
            if (lower.endsWith("mi")) return Long.parseLong(memStr.substring(0, memStr.length() - 2)) * 1024 * 1024;
            if (lower.endsWith("gi")) return Long.parseLong(memStr.substring(0, memStr.length() - 2)) * 1024 * 1024 * 1024;
            return Long.parseLong(memStr); // bytes
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void addAlertIfNew(Map<String, Alert> alertMap, List<Alert> newAlerts, 
                               Alert.Severity severity, Alert.Category category, 
                               String message, String resourceType, String resourceName) {
        String key = resourceType + "/" + resourceName + "/" + message;

        if (!alertMap.containsKey(key)) {
            Alert alert = new Alert(severity, category, message, resourceType, resourceName);
            newAlerts.add(alert);
            alertMap.put(key, alert);
            log.warn("[ALERT] [{}] {} - {} ({})", severity, category, message, resourceName);
        }
    }
}
