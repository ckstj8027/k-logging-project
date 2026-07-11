package com.k8s.cnapp.msa.query.infrastructure.asset;

import com.k8s.cnapp.msa.query.model.NodeProfile;

import java.util.List;

/**
 * Out-Port: Node 자산 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface NodeProfileReader {
    List<NodeProfile> findAllByTenantId(Long tenantId);
}
