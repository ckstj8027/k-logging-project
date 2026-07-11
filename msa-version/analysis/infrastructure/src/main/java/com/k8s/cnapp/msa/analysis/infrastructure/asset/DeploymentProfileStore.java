package com.k8s.cnapp.msa.analysis.infrastructure.asset;

import com.k8s.cnapp.msa.analysis.model.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;

import java.util.Optional;

/**
 * Out-Port: Deployment 프로파일 영속성 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface DeploymentProfileStore {

    Optional<DeploymentProfile> findByTenantAndNamespaceAndName(Tenant tenant, String namespace, String name);

    DeploymentProfile save(DeploymentProfile profile);
}
