package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.common.model.Status;
import com.k8s.cnapp.msa.query.infrastructure.alert.AlertCachePort;
import com.k8s.cnapp.msa.query.infrastructure.alert.AlertReader;
import com.k8s.cnapp.msa.query.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

/**
 * 기존 AlertQueryService 의 캐시 우선(cache-first) 로직을 포트 기반으로 그대로 유지한다.
 * 캐시 히트 → 즉시 반환 / 미스 → DB 조회 후 TTL 5분으로 캐시 갱신.
 */
@Slf4j
@RequiredArgsConstructor
class AlertQueryService implements AlertQueryUseCase {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final AlertReader alertReader;
    private final AlertCachePort alertCachePort;

    @Override
    public List<Alert> getOpenAlerts(Long tenantId) {
        try {
            // 1. Check Redis Cache
            List<Alert> cachedAlerts = alertCachePort.get(tenantId);
            if (cachedAlerts != null) {
                log.info("Returning cached alerts for tenant: {} (Count: {})", tenantId, cachedAlerts.size());
                return cachedAlerts;
            }
        } catch (Exception e) {
            log.error("Redis connection error, falling back to DB", e);
        }

        // 2. Fetch from PostgreSQL
        log.info("Cache miss for tenant: {}. Fetching from DB.", tenantId);
        List<Alert> alerts = alertReader.findAllByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, Status.OPEN);

        // 3. Update Cache (TTL 5 minutes)
        try {
            alertCachePort.put(tenantId, alerts, CACHE_TTL);
        } catch (Exception e) {
            log.error("Failed to update Redis cache", e);
        }

        return alerts;
    }

    @Override
    public List<Alert> getOpenAlerts(Long tenantId, Long lastId, int size) {
        // 캐시 우선 전체 목록을 가져온 뒤 메모리에서 keyset 슬라이스한다
        // (캐시 무효화 로직과 페이징을 모두 보존하기 위한 구조)
        return getOpenAlerts(tenantId).stream()
                .sorted(Comparator.comparing(Alert::getId).reversed())
                .filter(a -> lastId == null || a.getId() < lastId)
                .limit(size)
                .toList();
    }
}
