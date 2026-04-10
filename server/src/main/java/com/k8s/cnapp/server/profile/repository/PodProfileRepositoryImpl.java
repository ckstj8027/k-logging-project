package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.PodProfile;
import com.k8s.cnapp.server.profile.domain.QPodProfile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PodProfileRepositoryImpl implements PodProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PodProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize) {
        QPodProfile podProfile = QPodProfile.podProfile;

        return queryFactory
                .selectFrom(podProfile)
                .where(
                        podProfile.tenant.eq(tenant),
                        lastIdLt(lastId)
                )
                .orderBy(podProfile.lastSeenAt.desc(), podProfile.id.desc()) // 최근 수집순 정렬
                .limit(pageSize)

                .fetch();
    }

    private BooleanExpression lastIdLt(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return QPodProfile.podProfile.id.lt(lastId);
    }
}
