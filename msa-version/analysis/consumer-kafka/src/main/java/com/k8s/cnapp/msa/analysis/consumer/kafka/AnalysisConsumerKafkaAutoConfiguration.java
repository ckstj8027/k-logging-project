package com.k8s.cnapp.msa.analysis.consumer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.msa.analysis.service.ProcessSnapshotUseCase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AnalysisConsumerKafkaAutoConfiguration {

    @Bean
    public SnapshotParser snapshotParser(ObjectMapper objectMapper) {
        return new SnapshotParser(objectMapper);
    }

    // @KafkaListener 는 빈으로 등록되기만 하면 Boot 가 자동 감지한다.
    @Bean
    public AnalysisWorker analysisWorker(ObjectMapper objectMapper, SnapshotParser snapshotParser, ProcessSnapshotUseCase processSnapshotUseCase) {
        return new AnalysisWorker(objectMapper, snapshotParser, processSnapshotUseCase);
    }
}
