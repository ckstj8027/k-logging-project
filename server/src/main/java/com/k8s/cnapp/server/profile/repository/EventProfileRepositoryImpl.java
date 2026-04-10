package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.EventProfile;
import com.k8s.cnapp.server.profile.domain.QEventProfile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EventProfileRepositoryImpl implements EventProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EventProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize) {
        QEventProfile eventProfile = QEventProfile.eventProfile;

        return queryFactory
                .selectFrom(eventProfile)
                .where(
                        eventProfile.tenant.eq(tenant),
                        lastIdLt(lastId)
                )
                .orderBy(eventProfile.lastSeenAt.desc(), eventProfile.id.desc())
                .limit(pageSize)

                .fetch();
    }

    private BooleanExpression lastIdLt(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return QEventProfile.eventProfile.id.lt(lastId);
    }
}
