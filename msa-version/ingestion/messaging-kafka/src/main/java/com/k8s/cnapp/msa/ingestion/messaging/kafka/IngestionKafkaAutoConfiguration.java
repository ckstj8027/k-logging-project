package com.k8s.cnapp.msa.ingestion.messaging.kafka;

import com.k8s.cnapp.msa.common.dto.IngestionRequestMessage;
import com.k8s.cnapp.msa.ingestion.infrastructure.event.SnapshotEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
public class IngestionKafkaAutoConfiguration {

    public static final String INGESTION_TOPIC = "k8s.resource.ingestion";

    @Bean
    public NewTopic ingestionTopic() {
        return TopicBuilder.name(INGESTION_TOPIC)
                .partitions(3)    // 병렬 처리를 위해 파티션 3개 생성
                .replicas(1)      // 테스트 환경이므로 복제본 1개
                .build();
    }

    @Bean
    public SnapshotEventPublisher snapshotEventPublisher(KafkaTemplate<String, IngestionRequestMessage> kafkaTemplate) {
        return new KafkaSnapshotEventPublisher(kafkaTemplate);
    }
}
