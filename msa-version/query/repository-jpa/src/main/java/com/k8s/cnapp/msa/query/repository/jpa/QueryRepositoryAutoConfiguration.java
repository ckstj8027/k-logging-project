package com.k8s.cnapp.msa.query.repository.jpa;

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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EntityScan(basePackageClasses = QueryRepositoryAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = QueryRepositoryAutoConfiguration.class)
public class QueryRepositoryAutoConfiguration {

    @Bean
    public AlertReader alertReader(AlertJpaRepository alertJpaRepository) {
        return new AlertReaderJpaAdapter(alertJpaRepository);
    }

    @Bean
    public PodProfileReader podProfileReader(PodProfileJpaRepository podProfileJpaRepository) {
        return new PodProfileReaderJpaAdapter(podProfileJpaRepository);
    }

    @Bean
    public NodeProfileReader nodeProfileReader(NodeProfileJpaRepository nodeProfileJpaRepository) {
        return new NodeProfileReaderJpaAdapter(nodeProfileJpaRepository);
    }

    @Bean
    public ServiceProfileReader serviceProfileReader(ServiceProfileJpaRepository serviceProfileJpaRepository) {
        return new ServiceProfileReaderJpaAdapter(serviceProfileJpaRepository);
    }

    @Bean
    public DeploymentProfileReader deploymentProfileReader(DeploymentProfileJpaRepository deploymentProfileJpaRepository) {
        return new DeploymentProfileReaderJpaAdapter(deploymentProfileJpaRepository);
    }

    @Bean
    public NamespaceProfileReader namespaceProfileReader(NamespaceProfileJpaRepository namespaceProfileJpaRepository) {
        return new NamespaceProfileReaderJpaAdapter(namespaceProfileJpaRepository);
    }

    @Bean
    public EventProfileReader eventProfileReader(EventProfileJpaRepository eventProfileJpaRepository) {
        return new EventProfileReaderJpaAdapter(eventProfileJpaRepository);
    }

    @Bean
    public PolicyReader policyReader(PolicyJpaRepository policyJpaRepository) {
        return new PolicyReaderJpaAdapter(policyJpaRepository);
    }

    @Bean
    public PolicyWriter policyWriter(PolicyJpaRepository policyJpaRepository) {
        return new PolicyWriterJpaAdapter(policyJpaRepository);
    }
}
