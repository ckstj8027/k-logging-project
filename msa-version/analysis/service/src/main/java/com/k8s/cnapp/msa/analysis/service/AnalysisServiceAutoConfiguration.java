package com.k8s.cnapp.msa.analysis.service;

import com.k8s.cnapp.msa.analysis.infrastructure.alert.AlertWriter;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.DeploymentProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.EventProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.NamespaceProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.NodeProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.PodProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.ServiceProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.cache.AlertCacheInvalidationPort;
import com.k8s.cnapp.msa.analysis.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.analysis.infrastructure.tenant.TenantStore;
import com.k8s.cnapp.msa.analysis.model.policy.SecurityPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.DeploymentDefaultNamespacePolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.DeploymentMaxReplicaPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.DeploymentMinReplicaPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.NodeCpuLimitPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.NodeMemoryLimitPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.PodDefaultNamespacePolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.PodImageLatestTagPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.PodPrivilegedPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.PodRunAsRootPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.ServiceExternalIpPolicy;
import com.k8s.cnapp.msa.analysis.model.policy.impl.ServicePortBlacklistPolicy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class AnalysisServiceAutoConfiguration {

    /**
     * 11개 보안 정책을 순수 객체로 생성해 엔진에 주입한다 (기존 @Component 스캔 대체).
     */
    @Bean
    public PolicyEngine policyEngine() {
        return new PolicyEngine(List.<SecurityPolicy<?>>of(
                new PodPrivilegedPolicy(),
                new PodRunAsRootPolicy(),
                new PodImageLatestTagPolicy(),
                new PodDefaultNamespacePolicy(),
                new ServiceExternalIpPolicy(),
                new ServicePortBlacklistPolicy(),
                new DeploymentMinReplicaPolicy(),
                new DeploymentMaxReplicaPolicy(),
                new DeploymentDefaultNamespacePolicy(),
                new NodeCpuLimitPolicy(),
                new NodeMemoryLimitPolicy()
        ));
    }

    @Bean
    public ProcessSnapshotUseCase processSnapshotUseCase(
            TenantStore tenantStore,
            PolicyReader policyReader,
            PodProfileStore podProfileStore,
            NodeProfileStore nodeProfileStore,
            ServiceProfileStore serviceProfileStore,
            DeploymentProfileStore deploymentProfileStore,
            NamespaceProfileStore namespaceProfileStore,
            EventProfileStore eventProfileStore,
            AlertWriter alertWriter,
            PolicyEngine policyEngine,
            AlertCacheInvalidationPort alertCacheInvalidationPort
    ) {
        return new ProcessSnapshotService(
                tenantStore,
                policyReader,
                podProfileStore,
                nodeProfileStore,
                serviceProfileStore,
                deploymentProfileStore,
                namespaceProfileStore,
                eventProfileStore,
                alertWriter,
                policyEngine,
                alertCacheInvalidationPort
        );
    }
}
