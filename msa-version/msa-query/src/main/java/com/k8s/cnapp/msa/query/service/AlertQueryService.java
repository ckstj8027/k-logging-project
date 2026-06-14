package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.common.model.Status;
import com.k8s.cnapp.msa.query.domain.Alert;
import com.k8s.cnapp.msa.query.repository.AlertQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertQueryService {

    private final AlertQueryRepository alertRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "alerts:tenant:";

    @SuppressWarnings("unchecked")
    public List<Alert> getOpenAlerts(Long tenantId) {
        String cacheKey = CACHE_KEY_PREFIX + tenantId;
        
        try {
            // 1. Check Redis Cache
            List<Alert> cachedAlerts = (List<Alert>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedAlerts != null) {
                log.info("Returning cached alerts for tenant: {} (Count: {})", tenantId, cachedAlerts.size());
                return cachedAlerts;
            }
        } catch (Exception e) {
            log.error("Redis connection error, falling back to DB", e);
        }

        // 2. Fetch from PostgreSQL
        log.info("Cache miss for tenant: {}. Fetching from DB.", tenantId);
        List<Alert> alerts = alertRepository.findAllByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, Status.OPEN);

        // 3. Update Cache (TTL 5 minutes)
        try {
            redisTemplate.opsForValue().set(cacheKey, alerts, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.error("Failed to update Redis cache", e);
        }
        
        return alerts;
    }
}
