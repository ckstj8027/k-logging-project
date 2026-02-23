package com.k8s.cnapp.server.detection.service;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.alert.service.AlertService;
import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import com.k8s.cnapp.server.profile.domain.PodProfile;
import com.k8s.cnapp.server.profile.domain.ServicePortProfile;
import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import com.k8s.cnapp.server.profile.repository.DeploymentProfileRepository;
import com.k8s.cnapp.server.profile.repository.PodProfileRepository;
import com.k8s.cnapp.server.profile.repository.ServiceProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityScannerService {

    private final PodProfileRepository podProfileRepository;
    private final ServiceProfileRepository serviceProfileRepository;
    private final DeploymentProfileRepository deploymentProfileRepository;
    private final AlertService alertService;

    // 위험 포트 목록 (SSH, Telnet, SMTP, DNS, POP3, IMAP, LDAP, SMB, RDP, DB 등)
    private static final Set<Integer> DANGEROUS_PORTS = Set.of(
            21, 22, 23, 25, 53, 110, 143, 389, 445, 3306, 3389, 5432, 6379, 27017
    );

    // 1분마다 스캔 실행
    @Scheduled(fixedRate = 60000)
    @Transactional // readOnly = true 제거 (Alert 저장을 위해)
    public void scan() {
        log.info("Starting security scan...");
        try {
            scanPods();
            scanServices();
            scanDeployments();
            log.info("Security scan completed.");
        } catch (Exception e) {
            log.error("Error during security scan", e);
        }
    }

    private void scanPods() {
        List<PodProfile> pods = podProfileRepository.findAll();
        log.info("Scanning {} pods...", pods.size()); // 스캔 대상 개수 로그 추가
        for (PodProfile pod : pods) {
            String resourceName = pod.getAssetContext().getAssetKey();

            // 1. Privileged Pod 탐지 (Critical)
            if (Boolean.TRUE.equals(pod.getPrivileged())) {
                alertService.createAlert(Alert.Severity.CRITICAL, Alert.Category.CSPM,
                        "Privileged container detected", "Pod", resourceName);
            }

            // 2. Root 실행 탐지 (High)
            if (Boolean.TRUE.equals(pod.getRunAsRoot())) {
                alertService.createAlert(Alert.Severity.HIGH, Alert.Category.CSPM,
                        "Container running as root", "Pod", resourceName);
            }

            // 3. Default Namespace 사용 (Low)
            if ("default".equals(pod.getAssetContext().getNamespace())) {
                alertService.createAlert(Alert.Severity.LOW, Alert.Category.CSPM,
                        "Pod running in default namespace", "Pod", resourceName);
            }
            
            // 4. 이미지 태그 검사 (latest 사용)
            if (pod.getAssetContext().getImage() != null && pod.getAssetContext().getImage().endsWith(":latest")) {
                 alertService.createAlert(Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Image using 'latest' tag", "Pod", resourceName);
            }
        }
    }

    private void scanServices() {
        List<ServiceProfile> services = serviceProfileRepository.findAll();
        log.info("Scanning {} services...", services.size());
        for (ServiceProfile service : services) {
            String resourceName = service.getNamespace() + "/" + service.getName();

            // 1. 외부 노출 서비스 탐지 (Medium)
            if ("LoadBalancer".equals(service.getType()) || "NodePort".equals(service.getType())) {
                alertService.createAlert(Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Service exposed externally (" + service.getType() + ")", "Service", resourceName);
                
                // 2. 위험 포트 노출 탐지 (High) - 외부 노출된 경우에만 검사
                for (ServicePortProfile port : service.getPorts()) {
                    if (port.getPort() != null && DANGEROUS_PORTS.contains(port.getPort())) {
                        alertService.createAlert(Alert.Severity.HIGH, Alert.Category.CSPM,
                                "Dangerous port exposed: " + port.getPort() + " (" + port.getProtocol() + ")", 
                                "Service", resourceName);
                    }
                }
            }
        }
    }

    private void scanDeployments() {
        List<DeploymentProfile> deployments = deploymentProfileRepository.findAll();
        log.info("Scanning {} deployments...", deployments.size());
        for (DeploymentProfile deployment : deployments) {
            String resourceName = deployment.getNamespace() + "/" + deployment.getName();

            // 1. 레플리카 수 검사 (3개 이상이면 경고)
            if (deployment.getReplicas() != null && deployment.getReplicas() >= 3) {
                alertService.createAlert(Alert.Severity.LOW, Alert.Category.CSPM,
                        "High replica count detected (" + deployment.getReplicas() + ")", "Deployment", resourceName);
            }
            
            // 2. 레플리카 0개 검사 (서비스 중단 위험)
            if (deployment.getReplicas() != null && deployment.getReplicas() == 0) {
                alertService.createAlert(Alert.Severity.MEDIUM, Alert.Category.CSPM,
                        "Zero replicas detected (Service might be down)", "Deployment", resourceName);
            }
        }
    }
}
