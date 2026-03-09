package com.k8s.cnapp.server.detection.policy;

import com.k8s.cnapp.server.alert.domain.Alert;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PolicyEvaluationResult {
    private final boolean violated;
    private final Alert.Severity severity;
    private final String message;

    public static PolicyEvaluationResult success() {
        return PolicyEvaluationResult.builder().violated(false).build();
    }

    public static PolicyEvaluationResult failure(Alert.Severity severity, String message) {
        return PolicyEvaluationResult.builder()
                .violated(true)
                .severity(severity)
                .message(message)
                .build();
    }
}
