package com.k8s.cnapp.msa.query.infrastructure.asset;

import com.k8s.cnapp.msa.query.model.DeploymentProfile;

import java.util.List;

/**
 * Out-Port: Deployment 자산 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface DeploymentProfileReader {
    List<DeploymentProfile> findAllByTenantId(Long tenantId);
}
