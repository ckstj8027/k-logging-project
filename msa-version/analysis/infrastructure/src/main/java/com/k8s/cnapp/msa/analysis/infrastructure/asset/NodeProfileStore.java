package com.k8s.cnapp.msa.analysis.infrastructure.asset;

import com.k8s.cnapp.msa.analysis.model.NodeProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;

import java.util.Optional;

/**
 * Out-Port: Node 프로파일 영속성 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface NodeProfileStore {

    Optional<NodeProfile> findByTenantAndName(Tenant tenant, String name);

    NodeProfile save(NodeProfile profile);
}
