package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.query.infrastructure.policy.PolicyWriter;
import com.k8s.cnapp.msa.query.model.Policy;
import lombok.RequiredArgsConstructor;

/**
 * 기존 컨트롤러의 policyRepository.save(policy) 를 대체하는 명시적 저장 어댑터.
 * (@Id 가 지정된 엔티티 save → merge → UPDATE. 기존 갱신 의미 보존)
 */
@RequiredArgsConstructor
class PolicyWriterJpaAdapter implements PolicyWriter {

    private final PolicyJpaRepository policyJpaRepository;

    @Override
    public Policy save(Policy policy) {
        return policyJpaRepository.save(PolicyEntity.fromModel(policy)).toModel();
    }
}
