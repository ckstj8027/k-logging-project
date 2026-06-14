package com.k8s.cnapp.msa.ingestion.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String INGESTION_TOPIC = "k8s.resource.ingestion";

    @Bean
    public NewTopic ingestionTopic() {
        return TopicBuilder.name(INGESTION_TOPIC)
                .partitions(3)    // 병렬 처리를 위해 파티션 3개 생성
                .replicas(1)      // 테스트 환경이므로 복제본 1개
                .build();
    }
}
