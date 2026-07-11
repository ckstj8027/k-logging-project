package com.k8s.cnapp.msa.analysis.infrastructure.asset;

import com.k8s.cnapp.msa.analysis.model.PodProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;

import java.util.Optional;

/**
 * Out-Port: Pod 프로파일 영속성 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface PodProfileStore {

    Optional<PodProfile> findByTenantAndAsset(Tenant tenant, String namespace, String podName, String containerName);

    PodProfile save(PodProfile profile);
}
