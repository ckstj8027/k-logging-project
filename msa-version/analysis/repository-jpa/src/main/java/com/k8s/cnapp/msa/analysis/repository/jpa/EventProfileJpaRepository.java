package com.k8s.cnapp.msa.analysis.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface EventProfileJpaRepository extends JpaRepository<EventProfileEntity, Long> {
    // [해결] No property 'name' found 에러 해결. K8s 이벤트의 고유 식별자인 UID를 사용합니다.
    Optional<EventProfileEntity> findByTenantAndUid(TenantEntity tenant, String uid);
}
