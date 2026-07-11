package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.alert.AlertWriter;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.DeploymentProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.EventProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.NamespaceProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.NodeProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.PodProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.asset.ServiceProfileStore;
import com.k8s.cnapp.msa.analysis.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.analysis.infrastructure.tenant.TenantStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EntityScan(basePackageClasses = AnalysisRepositoryAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = AnalysisRepositoryAutoConfiguration.class)
public class AnalysisRepositoryAutoConfiguration {

    @Bean
    public TenantStore tenantStore(TenantJpaRepository tenantJpaRepository) {
        return new TenantStoreJpaAdapter(tenantJpaRepository);
    }

    @Bean
    public PolicyReader policyReader(PolicyJpaRepository policyJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new PolicyReaderJpaAdapter(policyJpaRepository, tenantJpaRepository);
    }

    @Bean
    public AlertWriter alertWriter(AlertJpaRepository alertJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new AlertWriterJpaAdapter(alertJpaRepository, tenantJpaRepository);
    }

    @Bean
    public PodProfileStore podProfileStore(PodProfileJpaRepository podProfileJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new PodProfileStoreJpaAdapter(podProfileJpaRepository, tenantJpaRepository);
    }

    @Bean
    public NodeProfileStore nodeProfileStore(NodeProfileJpaRepository nodeProfileJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new NodeProfileStoreJpaAdapter(nodeProfileJpaRepository, tenantJpaRepository);
    }

    @Bean
    public ServiceProfileStore serviceProfileStore(ServiceProfileJpaRepository serviceProfileJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new ServiceProfileStoreJpaAdapter(serviceProfileJpaRepository, tenantJpaRepository);
    }

    @Bean
    public DeploymentProfileStore deploymentProfileStore(DeploymentProfileJpaRepository deploymentProfileJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new DeploymentProfileStoreJpaAdapter(deploymentProfileJpaRepository, tenantJpaRepository);
    }

    @Bean
    public NamespaceProfileStore namespaceProfileStore(NamespaceProfileJpaRepository namespaceProfileJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new NamespaceProfileStoreJpaAdapter(namespaceProfileJpaRepository, tenantJpaRepository);
    }

    @Bean
    public EventProfileStore eventProfileStore(EventProfileJpaRepository eventProfileJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new EventProfileStoreJpaAdapter(eventProfileJpaRepository, tenantJpaRepository);
    }
}
