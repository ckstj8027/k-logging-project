package com.k8s.cnapp.msa.query.cache.redis;

import com.k8s.cnapp.msa.query.infrastructure.alert.AlertCachePort;
import com.k8s.cnapp.msa.query.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;

/**
 * 기존 AlertQueryService 와 동일한 키 형식("alerts:tenant:{tenantId}")과 TTL 을 사용한다.
 */
@Slf4j
@RequiredArgsConstructor
class AlertRedisCacheAdapter implements AlertCachePort {

    private static final String CACHE_KEY_PREFIX = "alerts:tenant:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<Alert> get(Long tenantId) {
        try {
            return (List<Alert>) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + tenantId);
        } catch (Exception e) {
            // 리팩터링 이전 캐시 항목은 @class 에 구(舊) FQCN 이 들어있어 역직렬화에 실패한다.
            // 어떤 예외든 캐시 미스로 간주하고 DB 조회로 폴백한다.
            log.warn("Failed to read alert cache for tenant: {}. Treating as cache miss.", tenantId, e);
            return null;
        }
    }

    @Override
    public void put(Long tenantId, List<Alert> alerts, Duration ttl) {
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + tenantId, alerts, ttl);
    }
}
