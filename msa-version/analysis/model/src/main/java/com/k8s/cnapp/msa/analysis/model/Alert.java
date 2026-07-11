package com.k8s.cnapp.msa.analysis.model;

import com.k8s.cnapp.msa.common.model.Category;
import com.k8s.cnapp.msa.common.model.Severity;
import com.k8s.cnapp.msa.common.model.Status;
import lombok.Getter;

/**
 * 정책 위반 알림 도메인 모델 (순수 POJO). 생성 시 상태는 항상 OPEN.
 */
@Getter
public class Alert {

    private final Tenant tenant;
    private final Severity severity;
    private final Category category;
    private final String message;
    private final String resourceType;
    private final String resourceName;
    private final Status status;

    public Alert(Tenant tenant, Severity severity, Category category, String message, String resourceType, String resourceName) {
        this.tenant = tenant;
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.status = Status.OPEN;
    }
}
