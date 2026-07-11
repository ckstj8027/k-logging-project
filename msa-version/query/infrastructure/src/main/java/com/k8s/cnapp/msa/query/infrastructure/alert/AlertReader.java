package com.k8s.cnapp.msa.query.infrastructure.alert;

import com.k8s.cnapp.msa.common.model.Status;
import com.k8s.cnapp.msa.query.model.Alert;

import java.util.List;

/**
 * Out-Port: 알림 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface AlertReader {
    List<Alert> findAllByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, Status status);
}
