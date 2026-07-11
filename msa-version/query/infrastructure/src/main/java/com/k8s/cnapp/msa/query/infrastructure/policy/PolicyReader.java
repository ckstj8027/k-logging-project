package com.k8s.cnapp.msa.query.infrastructure.policy;

import com.k8s.cnapp.msa.query.model.Policy;

import java.util.List;
import java.util.Optional;

/**
 * Out-Port: 정책 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface PolicyReader {
    List<Policy> findAllByTenantId(Long tenantId);

    Optional<Policy> findById(Long id);
}
