package com.k8s.cnapp.msa.analysis.infrastructure.alert;

import com.k8s.cnapp.msa.analysis.model.Alert;

/**
 * Out-Port: 정책 위반 알림 저장 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface AlertWriter {

    void save(Alert alert);
}
