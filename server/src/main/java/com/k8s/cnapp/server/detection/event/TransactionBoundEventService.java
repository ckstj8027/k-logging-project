package com.k8s.cnapp.server.detection.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionBoundEventService {

    private final SecurityEventPublisher rabbitPublisher;

    /**
     * 트랜잭션 커밋 이후에만 실행됨.
     * 이를 통해 RabbitMQ 컨슈머가 DB에서 최신 데이터를 조회할 수 있음을 보장함.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScanRequest(ScanRequestEvent event) {
        log.info("Transaction committed for tenant {}. Triggering RabbitMQ scan request.", event.getTenantId());
        rabbitPublisher.publishScanRequest(event);
    }
}
