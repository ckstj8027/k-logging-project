package com.k8s.cnapp.msa.analysis.infrastructure.asset;

import com.k8s.cnapp.msa.analysis.model.EventProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;

import java.util.Optional;

/**
 * Out-Port: Event 프로파일 영속성 규격. 구현은 repository-jpa 어댑터가 제공한다.
 * K8s 이벤트의 고유 식별자인 UID 로 조회한다.
 */
public interface EventProfileStore {

    Optional<EventProfile> findByTenantAndUid(Tenant tenant, String uid);

    EventProfile save(EventProfile profile);
}
