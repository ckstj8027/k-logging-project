package com.k8s.cnapp.msa.query.infrastructure.asset;

import com.k8s.cnapp.msa.query.model.NamespaceProfile;

import java.util.List;

/**
 * Out-Port: Namespace 자산 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface NamespaceProfileReader {
    List<NamespaceProfile> findAllByTenantId(Long tenantId);

    /**
     * 노오프셋(keyset) 페이징: id < lastId 조건 + id 내림차순 + size 개 제한.
     * lastId 가 null 이면 최신부터 size 개.
     */
    List<NamespaceProfile> findPage(Long tenantId, Long lastId, int size);
}
