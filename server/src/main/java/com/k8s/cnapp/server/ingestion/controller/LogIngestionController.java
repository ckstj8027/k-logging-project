package com.k8s.cnapp.server.ingestion.controller;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.auth.service.AuthService;
import com.k8s.cnapp.server.config.RabbitConfig;
import com.k8s.cnapp.server.ingestion.dto.IngestionRequestMessage;
import com.k8s.cnapp.server.ingestion.port.LogIngestionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class LogIngestionController implements LogIngestionPort {

    private final RabbitTemplate rabbitTemplate;
    private final AuthService authService;

    @PostMapping("/raw")
    @Override
    public void ingest(@RequestBody String rawData) {
        Tenant tenant = authService.getCurrentTenant();
        if (tenant == null) {
            log.warn("Unauthorized ingestion request attempt.");
            return;
        }

        // 1. 비동기 처리를 위해 RabbitMQ 큐로 데이터 전송
        IngestionRequestMessage message = new IngestionRequestMessage(tenant.getId(), rawData);
        rabbitTemplate.convertAndSend(RabbitConfig.INGESTION_RAW_QUEUE, message);
        
        log.debug("Ingestion request queued for tenant: {}", tenant.getName());
    }
}
