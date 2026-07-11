package com.k8s.cnapp.msa.auth.infrastructure.policy;

import com.k8s.cnapp.msa.auth.model.Policy;

/**
 * Out-Port: 정책 저장 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface PolicyWriter {
    void save(Policy policy);
}
