package com.k8s.cnapp.msa.analysis.cache.redis;

import com.k8s.cnapp.msa.analysis.infrastructure.cache.AlertCacheInvalidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 기존 AnalysisService 와 동일한 키(alerts:tenant:{tenantId})를 삭제한다.
 */
@RequiredArgsConstructor
class RedisAlertCacheInvalidationAdapter implements AlertCacheInvalidationPort {

    private static final String CACHE_KEY_PREFIX = "alerts:tenant:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void invalidate(Long tenantId) {
        redisTemplate.delete(CACHE_KEY_PREFIX + tenantId);
    }
}
