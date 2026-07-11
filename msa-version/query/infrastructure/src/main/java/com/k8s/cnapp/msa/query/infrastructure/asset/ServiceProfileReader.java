package com.k8s.cnapp.msa.query.infrastructure.asset;

import com.k8s.cnapp.msa.query.model.ServiceProfile;

import java.util.List;

/**
 * Out-Port: Service 자산 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface ServiceProfileReader {
    List<ServiceProfile> findAllByTenantId(Long tenantId);
}
