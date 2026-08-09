package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.model.Alert;

import java.util.List;

/**
 * In-Port: 미해결(OPEN) 알림 조회 유스케이스 (Redis 캐시 우선).
 */
public interface AlertQueryUseCase {
    List<Alert> getOpenAlerts(Long tenantId);

    /** 노오프셋 페이징: 캐시/DB의 전체 목록에서 id < lastId 최신순 size 개를 잘라 반환한다. */
    List<Alert> getOpenAlerts(Long tenantId, Long lastId, int size);
}
