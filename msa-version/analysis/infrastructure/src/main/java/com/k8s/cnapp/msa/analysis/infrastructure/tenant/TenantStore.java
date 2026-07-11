package com.k8s.cnapp.msa.analysis.infrastructure.tenant;

import com.k8s.cnapp.msa.analysis.model.Tenant;

import java.util.Optional;

/**
 * Out-Port: 테넌트 조회/생성 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface TenantStore {

    Optional<Tenant> findById(Long id);

    Tenant save(Tenant tenant);
}
