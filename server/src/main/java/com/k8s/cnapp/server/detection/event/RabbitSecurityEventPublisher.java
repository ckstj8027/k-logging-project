package com.k8s.cnapp.server.detection.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitSecurityEventPublisher implements SecurityEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    public static final String SCAN_QUEUE = "security.scan.requests";

    @Override
    @Async("scanExecutor")
    public void publishScanRequest(ScanRequestEvent event) {
        log.info("Publishing targeted scan request for tenant: {} (Types: {})", 
                event.getTenantId(), event.getUpdatedResourceIds().keySet());
        try {
            rabbitTemplate.convertAndSend(SCAN_QUEUE, event);
        } catch (Exception e) {
            log.error("Failed to publish scan request to RabbitMQ for tenant: {}", event.getTenantId(), e);
        }
    }
}
