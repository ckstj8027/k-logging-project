package com.k8s.cnapp.msa.ingestion.controller;

import com.k8s.cnapp.msa.common.dto.IngestionRequestMessage;
import com.k8s.cnapp.msa.ingestion.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/k8s")
    public ResponseEntity<Void> ingestK8sData(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestBody String rawData) {
        
        log.info("Received ingestion request with API Key: {}", apiKey);
        
        // TODO: Validate API Key and get Tenant ID from msa-auth or DB
        // For now, assuming tenantId 1 for demonstration
        Long tenantId = 1L;

        IngestionRequestMessage message = new IngestionRequestMessage(tenantId, rawData);
        kafkaProducerService.sendIngestionMessage(message);

        return ResponseEntity.ok().build();
    }
}
