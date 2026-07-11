package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.model.Policy;

import java.util.Optional;

/**
 * In-Port: 정책 수정 유스케이스 (PUT /api/policies/{id}).
 */
public interface PolicyUpdateUseCase {

    /**
     * @return 수정된 정책. 정책이 없거나 다른 테넌트 소유이면 Optional.empty()
     *         (컨트롤러가 403으로 응답한다 — 기존 동작 보존).
     */
    Optional<Policy> update(Long tenantId, Long policyId, String value, boolean enabled);
}
