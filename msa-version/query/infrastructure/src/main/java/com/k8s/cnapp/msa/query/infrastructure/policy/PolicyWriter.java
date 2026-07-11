package com.k8s.cnapp.msa.query.infrastructure.policy;

import com.k8s.cnapp.msa.query.model.Policy;

/**
 * Out-Port: 정책 저장 규격 (PUT /api/policies/{id} 용). 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface PolicyWriter {

    /**
     * @return 저장된 정책
     */
    Policy save(Policy policy);
}
