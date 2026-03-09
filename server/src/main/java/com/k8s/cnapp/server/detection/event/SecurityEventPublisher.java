package com.k8s.cnapp.server.detection.event;

/**
 * 보안 스캔 요청 등의 이벤트를 발행하는 인터페이스.
 * 추후 RabbitMQ에서 Kafka 등으로 기술 변경 시, 이 인터페이스의 구현체만 교체하면 됩니다.
 */
public interface SecurityEventPublisher {
    
    /**
     * 정밀 스캔 이벤트를 발행합니다.
     * @param event 테넌트 정보와 변경된 리소스 ID 리스트를 담은 이벤트
     */
    void publishScanRequest(ScanRequestEvent event);
}
