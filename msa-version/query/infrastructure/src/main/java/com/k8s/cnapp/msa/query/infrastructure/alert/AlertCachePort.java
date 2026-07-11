package com.k8s.cnapp.msa.query.infrastructure.alert;

import com.k8s.cnapp.msa.query.model.Alert;

import java.time.Duration;
import java.util.List;

/**
 * Out-Port: 알림 캐시 규격. 구현은 cache-redis 어댑터가 제공한다.
 */
public interface AlertCachePort {

    /**
     * @return 캐시된 알림 목록. 캐시 미스(역직렬화 실패 포함) 시 null.
     */
    List<Alert> get(Long tenantId);

    void put(Long tenantId, List<Alert> alerts, Duration ttl);
}
