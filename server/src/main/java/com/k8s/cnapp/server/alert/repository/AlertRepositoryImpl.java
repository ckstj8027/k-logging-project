package com.k8s.cnapp.server.alert.repository;

import com.k8s.cnapp.server.alert.domain.Alert;
import com.k8s.cnapp.server.alert.domain.QAlert;
import com.k8s.cnapp.server.auth.domain.Tenant;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AlertRepositoryImpl implements AlertRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Alert> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize) {
        QAlert alert = QAlert.alert;

        return queryFactory
                .selectFrom(alert)
                .where(
                        alert.tenant.eq(tenant),
                        ltLastId(lastId)
                )
                .orderBy(alert.createdAt.desc(), alert.id.desc()) // 발생 시간 기준 최신순 정렬
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression ltLastId(Long lastId) {
        if (lastId == null) {
            return null; // 첫 페이지 조회 시
        }
        return QAlert.alert.id.lt(lastId); // 마지막 본 ID보다 작은 ID들 조회 (최신순일 때)
    }
}
