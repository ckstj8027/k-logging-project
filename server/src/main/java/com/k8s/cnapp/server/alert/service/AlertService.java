package com.k8s.cnapp.server.alert.service;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional
    public void createAlert(Alert.Severity severity, Alert.Category category, String message, String resourceType, String resourceName) {
        // 중복 Alert 방지 (이미 OPEN 상태인 동일한 Alert가 있으면 생성하지 않음)
        List<Alert> existingAlerts = alertRepository.findByResourceTypeAndResourceNameAndStatus(resourceType, resourceName, Alert.Status.OPEN);
        
        // 메시지까지 동일한지 체크 (더 정교한 중복 제거)
        boolean isDuplicate = existingAlerts.stream()
                .anyMatch(a -> a.getMessage().equals(message));

        if (isDuplicate) {
            return;
        }

        Alert alert = new Alert(severity, category, message, resourceType, resourceName);
        alertRepository.save(alert);
        log.warn("[ALERT] [{}] {} - {} ({})", severity, category, message, resourceName);
    }
}
