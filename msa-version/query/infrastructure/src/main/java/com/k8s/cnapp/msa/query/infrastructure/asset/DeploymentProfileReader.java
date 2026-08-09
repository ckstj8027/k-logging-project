package com.k8s.cnapp.msa.query.infrastructure.asset;

import com.k8s.cnapp.msa.query.model.DeploymentProfile;

import java.util.List;

/**
 * Out-Port: Deployment 자산 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface DeploymentProfileReader {
    List<DeploymentProfile> findAllByTenantId(Long tenantId);

    /**
     * 노오프셋(keyset) 페이징: id < lastId 조건 + id 내림차순 + size 개 제한.
     * lastId 가 null 이면 최신부터 size 개.
     */
    List<DeploymentProfile> findPage(Long tenantId, Long lastId, int size);
}
