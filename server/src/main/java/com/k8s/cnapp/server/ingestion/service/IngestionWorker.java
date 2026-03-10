package com.k8s.cnapp.server.ingestion.service;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.repository.TenantRepository;
import com.k8s.cnapp.server.config.RabbitConfig;
import com.k8s.cnapp.server.ingestion.dto.IngestionRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionWorker {

    private final LogProcessingService logProcessingService;
    private final TenantRepository tenantRepository;

    @RabbitListener(queues = RabbitConfig.INGESTION_RAW_QUEUE, concurrency = "3-5")
    public void processIngestion(IngestionRequestMessage message) {
        log.debug("Worker received ingestion request for tenant ID: {}", message.tenantId());
        
        tenantRepository.findById(message.tenantId()).ifPresentOrElse(
            tenant -> {
                try {
                    logProcessingService.processRawData(tenant, message.rawData());
                } catch (Exception e) {
                    log.error("Failed to process background ingestion for tenant: {}", tenant.getName(), e);
                }
            },
            () -> log.warn("Tenant not found for ID: {}. Skipping ingestion.", message.tenantId())
        );
    }
}
