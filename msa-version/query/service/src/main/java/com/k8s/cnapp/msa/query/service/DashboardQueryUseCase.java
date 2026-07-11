package com.k8s.cnapp.msa.query.service;

import java.util.Map;

/**
 * In-Port: 대시보드 요약(자산/알림 카운트) 조회 유스케이스.
 */
public interface DashboardQueryUseCase {
    Map<String, Object> getSummary(Long tenantId);
}
