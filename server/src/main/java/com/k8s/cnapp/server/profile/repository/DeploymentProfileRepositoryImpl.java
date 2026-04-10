package com.k8s.cnapp.server.profile.repository;

import com.k8s.cnapp.server.auth.domain.Tenant;
import com.k8s.cnapp.server.profile.domain.DeploymentProfile;
import com.k8s.cnapp.server.profile.domain.QDeploymentProfile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DeploymentProfileRepositoryImpl implements DeploymentProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DeploymentProfile> findAllByTenantNoOffset(Tenant tenant, Long lastId, int pageSize) {
        QDeploymentProfile deploymentProfile = QDeploymentProfile.deploymentProfile;

        return queryFactory
                .selectFrom(deploymentProfile)
                .where(
                        deploymentProfile.tenant.eq(tenant),
                        lastIdLt(lastId)
                )
                .orderBy(deploymentProfile.lastSeenAt.desc(), deploymentProfile.id.desc())
                .limit(pageSize)

                .fetch();
    }

    private BooleanExpression lastIdLt(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return QDeploymentProfile.deploymentProfile.id.lt(lastId);
    }
}
