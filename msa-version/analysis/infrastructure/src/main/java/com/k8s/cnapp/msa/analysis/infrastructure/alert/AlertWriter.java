package com.k8s.cnapp.msa.analysis.infrastructure.alert;

import com.k8s.cnapp.msa.analysis.model.Alert;
import com.k8s.cnapp.msa.analysis.model.Tenant;

/**
 * Out-Port: 정책 위반 알림 저장 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface AlertWriter {

    void save(Alert alert);

    /** 동일 리소스의 동일 위반이 OPEN 상태로 이미 존재하는지 확인한다 (중복 알림 방지). */
    boolean existsOpen(Tenant tenant, String resourceType, String resourceName, String message);
}
