package com.k8s.cnapp.msa.ingestion.messaging.kafka;

import com.k8s.cnapp.msa.common.dto.IngestionRequestMessage;
import com.k8s.cnapp.msa.ingestion.infrastructure.event.SnapshotEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
class KafkaSnapshotEventPublisher implements SnapshotEventPublisher {

    private final KafkaTemplate<String, IngestionRequestMessage> kafkaTemplate;

    @Override
    public void publish(Long tenantId, String rawData) {
        IngestionRequestMessage message = new IngestionRequestMessage(tenantId, rawData);
        log.info("[KAFKA-PRODUCER] Attempting to send message for tenant {} to topic {}",
                message.tenantId(), IngestionKafkaAutoConfiguration.INGESTION_TOPIC);
        kafkaTemplate.send(IngestionKafkaAutoConfiguration.INGESTION_TOPIC, String.valueOf(message.tenantId()), message)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA-PRODUCER] Success: Sent to topic {} partition {} offset {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA-PRODUCER] Failure: {}", ex.getMessage());
                }
            });
    }
}
