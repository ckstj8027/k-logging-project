package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.NodeProfile;
import com.k8s.cnapp.server.profile.domain.QNodeProfile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class NodeProfileRepositoryImpl implements NodeProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NodeProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize) {
        QNodeProfile nodeProfile = QNodeProfile.nodeProfile;

        return queryFactory
                .selectFrom(nodeProfile)
                .where(
                        nodeProfile.tenant.eq(tenant),
                        lastIdLt(lastId)
                )
                .orderBy(nodeProfile.lastSeenAt.desc(), nodeProfile.id.desc())
                .limit(pageSize)

                .fetch();
    }

    private BooleanExpression lastIdLt(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return QNodeProfile.nodeProfile.id.lt(lastId);
    }
}
