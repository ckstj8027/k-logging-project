package com.k8s.cnapp.msa.ingestion.api;

import com.k8s.cnapp.msa.ingestion.exception.InvalidApiKeyException;
import com.k8s.cnapp.msa.ingestion.service.IngestSnapshotUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestSnapshotUseCase ingestSnapshotUseCase;

    @PostMapping("/k8s")
    public ResponseEntity<Void> ingestK8sData(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestBody String rawData) {

        log.info("[INGESTION] Received request with API Key: {}", apiKey);

        try {
            ingestSnapshotUseCase.ingest(apiKey, rawData);
        } catch (InvalidApiKeyException e) {
            log.warn("[INGESTION] Invalid API Key: {}. Data will be discarded unless fallback is used.", apiKey);
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("[INGESTION] Error sending to Kafka: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}
