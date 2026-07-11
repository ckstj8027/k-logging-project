package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.infrastructure.alert.AlertCachePort;
import com.k8s.cnapp.msa.query.infrastructure.alert.AlertReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.DeploymentProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.EventProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.NamespaceProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.NodeProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.PodProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.ServiceProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.query.infrastructure.policy.PolicyWriter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class QueryServiceAutoConfiguration {

    @Bean
    public AlertQueryUseCase alertQueryUseCase(AlertReader alertReader, AlertCachePort alertCachePort) {
        return new AlertQueryService(alertReader, alertCachePort);
    }

    @Bean
    public AssetQueryUseCase assetQueryUseCase(
            PodProfileReader podProfileReader,
            NodeProfileReader nodeProfileReader,
            ServiceProfileReader serviceProfileReader,
            DeploymentProfileReader deploymentProfileReader,
            NamespaceProfileReader namespaceProfileReader,
            EventProfileReader eventProfileReader
    ) {
        return new AssetQueryService(
                podProfileReader,
                nodeProfileReader,
                serviceProfileReader,
                deploymentProfileReader,
                namespaceProfileReader,
                eventProfileReader
        );
    }

    @Bean
    public DashboardQueryUseCase dashboardQueryUseCase(
            PodProfileReader podProfileReader,
            NodeProfileReader nodeProfileReader,
            ServiceProfileReader serviceProfileReader,
            DeploymentProfileReader deploymentProfileReader,
            NamespaceProfileReader namespaceProfileReader,
            EventProfileReader eventProfileReader,
            AlertQueryUseCase alertQueryUseCase
    ) {
        return new DashboardQueryService(
                podProfileReader,
                nodeProfileReader,
                serviceProfileReader,
                deploymentProfileReader,
                namespaceProfileReader,
                eventProfileReader,
                alertQueryUseCase
        );
    }

    @Bean
    public PolicyQueryUseCase policyQueryUseCase(PolicyReader policyReader) {
        return new PolicyQueryService(policyReader);
    }

    @Bean
    public PolicyUpdateUseCase policyUpdateUseCase(PolicyReader policyReader, PolicyWriter policyWriter) {
        return new PolicyUpdateService(policyReader, policyWriter);
    }
}
