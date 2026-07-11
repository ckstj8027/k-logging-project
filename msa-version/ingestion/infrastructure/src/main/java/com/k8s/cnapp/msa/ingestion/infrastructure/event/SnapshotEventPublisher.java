package com.k8s.cnapp.msa.ingestion.infrastructure.event;

/**
 * Out-Port: 수집 스냅샷 발행 규격. 구현은 messaging-kafka 어댑터가 제공한다.
 */
public interface SnapshotEventPublisher {
    void publish(Long tenantId, String rawData);
}
