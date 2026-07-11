package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.model.Alert;

import java.util.List;

/**
 * In-Port: 미해결(OPEN) 알림 조회 유스케이스 (Redis 캐시 우선).
 */
public interface AlertQueryUseCase {
    List<Alert> getOpenAlerts(Long tenantId);
}
