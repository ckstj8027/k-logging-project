package com.k8s.cnapp.msa.ingestion.infrastructure.tenant;

import com.k8s.cnapp.msa.ingestion.model.Tenant;

import java.util.Optional;

/**
 * Out-Port: 테넌트 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface TenantReader {
    Optional<Tenant> findByApiKey(String apiKey);
}
