package com.k8s.cnapp.msa.analysis.policy;

import com.k8s.cnapp.msa.common.model.Severity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PolicyEvaluationResult {
    private final boolean violated;
    private final Severity severity;
    private final String message;

    public static PolicyEvaluationResult success() {
        return PolicyEvaluationResult.builder().violated(false).build();
    }

    public static PolicyEvaluationResult failure(Severity severity, String message) {
        return PolicyEvaluationResult.builder()
                .violated(true)
                .severity(severity)
                .message(message)
                .build();
    }
}
