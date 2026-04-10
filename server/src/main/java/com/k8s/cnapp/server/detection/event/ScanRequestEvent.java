package com.k8s.cnapp.server.detection.event;

import com.k8s.cnapp.server.policy.domain.Policy;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 정밀 스캔을 위한 이벤트 메시지 DTO
 */
@Getter
@Builder
public class ScanRequestEvent implements Serializable {
    private final Long tenantId;
    private final boolean targetedScan; // 명시적 필드 추가
    
    // ResourceType별로 변경된 ID 리스트를 담음
    // 예: POD -> [1, 2, 3], SERVICE -> [10, 11]
    private final Map<Policy.ResourceType, List<Long>> updatedResourceIds;

    /**
     * 특정 리소스 ID들만 스캔할지 여부 확인
     */
    public boolean isTargetedScan() {
        return targetedScan && updatedResourceIds != null && !updatedResourceIds.isEmpty();
    }
}
