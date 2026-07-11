package com.k8s.cnapp.msa.analysis.infrastructure.cache;

/**
 * Out-Port: 테넌트 알림 캐시 무효화 규격. 구현은 cache-redis 어댑터가 제공한다.
 */
public interface AlertCacheInvalidationPort {

    void invalidate(Long tenantId);
}
