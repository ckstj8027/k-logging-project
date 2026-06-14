package com.k8s.cnapp.msa.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.msa.common.dto.ClusterSnapshot;
import com.k8s.cnapp.msa.common.dto.IngestionRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisWorker {

    private final ObjectMapper objectMapper;
    private final AnalysisService analysisService;

    @KafkaListener(topics = "k8s.resource.ingestion", groupId = "msa-analysis-group")
    public void consumeIngestion(IngestionRequestMessage message) {
        log.info("Consumed message for tenant: {}", message.tenantId());
        try {
            ClusterSnapshot snapshot = objectMapper.readValue(message.rawData(), ClusterSnapshot.class);
            analysisService.processSnapshot(message.tenantId(), snapshot);
        } catch (Exception e) {
            log.error("Failed to process ingested data", e);
        }
    }
}
