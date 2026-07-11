package com.k8s.cnapp.msa.ingestion.service;

import com.k8s.cnapp.msa.ingestion.infrastructure.event.SnapshotEventPublisher;
import com.k8s.cnapp.msa.ingestion.infrastructure.tenant.TenantReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class IngestionServiceAutoConfiguration {

    @Bean
    public IngestSnapshotUseCase ingestSnapshotUseCase(
            TenantReader tenantReader,
            SnapshotEventPublisher snapshotEventPublisher
    ) {
        return new IngestSnapshotService(tenantReader, snapshotEventPublisher);
    }
}
