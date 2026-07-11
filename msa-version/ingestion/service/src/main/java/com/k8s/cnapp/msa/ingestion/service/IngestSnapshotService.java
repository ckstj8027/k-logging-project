package com.k8s.cnapp.msa.ingestion.service;

import com.k8s.cnapp.msa.ingestion.exception.InvalidApiKeyException;
import com.k8s.cnapp.msa.ingestion.infrastructure.event.SnapshotEventPublisher;
import com.k8s.cnapp.msa.ingestion.infrastructure.tenant.TenantReader;
import com.k8s.cnapp.msa.ingestion.model.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class IngestSnapshotService implements IngestSnapshotUseCase {

    private final TenantReader tenantReader;
    private final SnapshotEventPublisher snapshotEventPublisher;

    @Override
    public void ingest(String apiKey, String rawData) {
        Long tenantId = tenantReader.findByApiKey(apiKey)
                .map(Tenant::id)
                .orElseThrow(() -> new InvalidApiKeyException(apiKey));

        snapshotEventPublisher.publish(tenantId, rawData);
        log.info("[INGESTION] Successfully sent data to Kafka for tenant: {}", tenantId);
    }
}
