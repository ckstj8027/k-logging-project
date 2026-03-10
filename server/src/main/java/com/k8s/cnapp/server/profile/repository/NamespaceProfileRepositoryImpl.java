package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.NamespaceProfile;
import com.k8s.cnapp.server.profile.domain.QNamespaceProfile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class NamespaceProfileRepositoryImpl implements NamespaceProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NamespaceProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize) {
        QNamespaceProfile namespaceProfile = QNamespaceProfile.namespaceProfile;

        return queryFactory
                .selectFrom(namespaceProfile)
                .where(
                        namespaceProfile.tenant.eq(tenant),
                        ltLastId(lastId)
                )
                .orderBy(namespaceProfile.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression ltLastId(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return QNamespaceProfile.namespaceProfile.id.lt(lastId);
    }
}
