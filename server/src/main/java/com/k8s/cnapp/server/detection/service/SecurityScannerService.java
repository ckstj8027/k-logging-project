package com.k8s.cnapp.server.detection.service;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.alert.repository.AlertRepository;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.policy.service.PolicyService;
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

import java.util.*;
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
    private final PolicyService policyService;

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
                            (existing, replacement) -> existing
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
        
        // 정책 로드
        boolean denyPrivileged = "true".equals(policyService.getPolicyValue(Policy.ResourceType.POD, Policy.RuleType.PRIVILEGED_DENY));
        boolean denyRunAsRoot = "true".equals(policyService.getPolicyValue(Policy.ResourceType.POD, Policy.RuleType.RUN_AS_ROOT_DENY));
        boolean denyLatestTag = "true".equals(policyService.getPolicyValue(Policy.ResourceType.POD, Policy.RuleType.IMAGE_LATEST_TAG_DENY));

        for (PodProfile pod : pods) {
            String resourceName = pod.getAssetContext().getAssetKey();

            if (denyPrivileged && Boolean.TRUE.equals(pod.getPrivileged())) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.CRITICAL, Alert.Category.CSPM,
                        "Privileged container detected", "Pod", resourceName);
            }

            if (denyRunAsRoot && Boolean.TRUE.equals(pod.getRunAsRoot())) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.HIGH, Alert.Category.CSPM,
                        "Container running as root", "Pod", resourceName);
            }

            if ("default".equals(pod.getAssetContext().getNamespace())) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.LOW, Alert.Category.CSPM,
                        "Pod running in default namespace", "Pod", resourceName);
            }
            
            if (denyLatestTag && pod.getAssetContext().getImage() != null && pod.getAssetContext().getImage().endsWith(":latest")) {
                 addAlertIfNew(alertMap, newAlerts, Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Image using 'latest' tag", "Pod", resourceName);
            }
        }
    }

    private void scanServices(Map<String, Alert> alertMap, List<Alert> newAlerts) {
        List<ServiceProfile> services = serviceProfileRepository.findAllWithPorts();
        log.info("Scanning {} services...", services.size());
        
        // 정책 로드
        String portBlacklistStr = policyService.getPolicyValue(Policy.ResourceType.SERVICE, Policy.RuleType.PORT_BLACKLIST);
        Set<Integer> dangerousPorts = new HashSet<>();
        if (portBlacklistStr != null && !portBlacklistStr.isEmpty()) {
            for (String p : portBlacklistStr.split(",")) {
                try {
                    dangerousPorts.add(Integer.parseInt(p.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        boolean denyExternalIp = "true".equals(policyService.getPolicyValue(Policy.ResourceType.SERVICE, Policy.RuleType.EXTERNAL_IP_DENY));

        for (ServiceProfile service : services) {
            String resourceName = service.getNamespace() + "/" + service.getName();

            if (denyExternalIp && ("LoadBalancer".equals(service.getType()) || "NodePort".equals(service.getType()))) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Service exposed externally (" + service.getType() + ")", "Service", resourceName);
                
                for (ServicePortProfile port : service.getPorts()) {
                    if (port.getPort() != null && dangerousPorts.contains(port.getPort())) {
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
        
        // 정책 로드
        int maxReplicas = 10;
        int minReplicas = 1;
        try {
            maxReplicas = Integer.parseInt(policyService.getPolicyValue(Policy.ResourceType.DEPLOYMENT, Policy.RuleType.REPLICA_MAX_LIMIT));
            minReplicas = Integer.parseInt(policyService.getPolicyValue(Policy.ResourceType.DEPLOYMENT, Policy.RuleType.REPLICA_MIN_LIMIT));
        } catch (NumberFormatException ignored) {}

        for (DeploymentProfile deployment : deployments) {
            String resourceName = deployment.getNamespace() + "/" + deployment.getName();

            if (deployment.getReplicas() != null && deployment.getReplicas() > maxReplicas) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.LOW, Alert.Category.CSPM,
                        "High replica count detected (" + deployment.getReplicas() + " > " + maxReplicas + ")", "Deployment", resourceName);
            }
            
            if (deployment.getReplicas() != null && deployment.getReplicas() < minReplicas) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Low replica count detected (" + deployment.getReplicas() + " < " + minReplicas + ")", "Deployment", resourceName);
            }
        }
    }

    private void scanNodes(Map<String, Alert> alertMap, List<Alert> newAlerts) {
        List<NodeProfile> nodes = nodeProfileRepository.findAll();
        log.info("Scanning {} nodes...", nodes.size());
        
        // 정책 로드
        int cpuLimit = 8;
        long memoryLimit = 32L * 1024 * 1024 * 1024;
        try {
            cpuLimit = Integer.parseInt(policyService.getPolicyValue(Policy.ResourceType.NODE, Policy.RuleType.CPU_LIMIT_CORES));
            memoryLimit = Long.parseLong(policyService.getPolicyValue(Policy.ResourceType.NODE, Policy.RuleType.MEMORY_LIMIT_BYTES));
        } catch (NumberFormatException ignored) {}

        for (NodeProfile node : nodes) {
            String resourceName = node.getName();

            // CPU Capacity Check
            long cpuCores = parseCpu(node.getCpuCapacity());
            if (cpuCores > cpuLimit) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.HIGH, Alert.Category.CSPM,
                        "Node CPU capacity exceeds limit (" + cpuCores + " > " + cpuLimit + ")", "Node", resourceName);
            }

            // Memory Capacity Check
            long memoryBytes = parseMemory(node.getMemoryCapacity());
            if (memoryBytes > memoryLimit) {
                addAlertIfNew(alertMap, newAlerts, Alert.Severity.HIGH, Alert.Category.CSPM,
                        "Node Memory capacity exceeds limit (" + (memoryBytes / 1024 / 1024 / 1024) + "GB > " + (memoryLimit / 1024 / 1024 / 1024) + "GB)", "Node", resourceName);
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
