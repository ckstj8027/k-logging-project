package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.QServiceProfile;
import com.k8s.cnapp.server.profile.domain.ServiceProfile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ServiceProfileRepositoryImpl implements ServiceProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ServiceProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize) {
        QServiceProfile serviceProfile = QServiceProfile.serviceProfile;

        return queryFactory
                .selectFrom(serviceProfile)
                .where(
                        serviceProfile.tenant.eq(tenant),
                        lastIdLt(lastId)
                )
                .orderBy(serviceProfile.lastSeenAt.desc(), serviceProfile.id.desc())
                .limit(pageSize)

                .fetch();
    }

    private BooleanExpression lastIdLt(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return QServiceProfile.serviceProfile.id.lt(lastId);
    }
}
