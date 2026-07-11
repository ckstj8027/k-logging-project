package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.query.infrastructure.policy.PolicyWriter;
import com.k8s.cnapp.msa.query.model.Policy;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * 기존 PolicyCommandController 의 수정 로직을 유지한다:
 * 조회 → 테넌트 소유 검증 → policy.update(value, enabled) → 명시적 save.
 */
@RequiredArgsConstructor
class PolicyUpdateService implements PolicyUpdateUseCase {

    private final PolicyReader policyReader;
    private final PolicyWriter policyWriter;

    @Override
    public Optional<Policy> update(Long tenantId, Long policyId, String value, boolean enabled) {
        Policy policy = policyReader.findById(policyId).orElse(null);
        if (policy == null || !policy.getTenantId().equals(tenantId)) {
            return Optional.empty();
        }

        policy.update(value, enabled);
        return Optional.of(policyWriter.save(policy));
    }
}
