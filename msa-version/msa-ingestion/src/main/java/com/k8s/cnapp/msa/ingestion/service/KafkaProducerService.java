package com.k8s.cnapp.msa.ingestion.service;

import com.k8s.cnapp.msa.common.dto.IngestionRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    public static final String INGESTION_TOPIC = "k8s.resource.ingestion";
    private final KafkaTemplate<String, IngestionRequestMessage> kafkaTemplate;

    public void sendIngestionMessage(IngestionRequestMessage message) {
        log.debug("Sending ingestion message to Kafka for tenant: {}", message.tenantId());
        kafkaTemplate.send(INGESTION_TOPIC, String.valueOf(message.tenantId()), message);
    }
}
