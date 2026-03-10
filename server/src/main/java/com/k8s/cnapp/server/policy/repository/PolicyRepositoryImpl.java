package com.k8s.cnapp.server.policy.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.policy.domain.Policy;
import com.k8s.cnapp.server.policy.domain.QPolicy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PolicyRepositoryImpl implements PolicyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Policy> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize) {
        QPolicy policy = QPolicy.policy;

        return queryFactory
                .selectFrom(policy)
                .where(
                        policy.tenant.eq(tenant),
                        ltLastId(lastId)
                )
                .orderBy(policy.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression ltLastId(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return QPolicy.policy.id.lt(lastId);
    }
}
