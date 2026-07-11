package com.k8s.cnapp.msa.analysis.consumer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.msa.analysis.service.ProcessSnapshotUseCase;
import com.k8s.cnapp.msa.common.dto.IngestionRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@RequiredArgsConstructor
class AnalysisWorker {

    private final ObjectMapper objectMapper;
    private final SnapshotParser snapshotParser;
    private final ProcessSnapshotUseCase processSnapshotUseCase;

    @KafkaListener(topics = "k8s.resource.ingestion", groupId = "msa-analysis-group")
    public void consumeIngestion(IngestionRequestMessage message) {
        log.info("[KAFKA-CONSUMER] === NEW MESSAGE RECEIVED for tenant: {} ===", message.tenantId());
        try {
            log.info("[KAFKA-CONSUMER] Parsing raw data (size: {} chars)", message.rawData().length());
            ClusterSnapshot snapshot = objectMapper.readValue(message.rawData(), ClusterSnapshot.class);
            log.info("[KAFKA-CONSUMER] Snapshot parsed. Delegating to AnalysisService...");
            processSnapshotUseCase.process(message.tenantId(), snapshotParser.parse(snapshot));
        } catch (Exception e) {
            log.error("[KAFKA-CONSUMER] FATAL ERROR during processing: {}", e.getMessage(), e);
        }
    }
}
