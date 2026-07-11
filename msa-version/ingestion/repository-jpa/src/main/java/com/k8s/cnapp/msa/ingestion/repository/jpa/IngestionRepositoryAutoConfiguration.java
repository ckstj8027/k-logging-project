package com.k8s.cnapp.msa.ingestion.repository.jpa;

import com.k8s.cnapp.msa.ingestion.infrastructure.tenant.TenantReader;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EntityScan(basePackageClasses = IngestionRepositoryAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = IngestionRepositoryAutoConfiguration.class)
public class IngestionRepositoryAutoConfiguration {

    @Bean
    public TenantReader tenantReader(TenantJpaRepository tenantJpaRepository) {
        return new TenantReaderJpaAdapter(tenantJpaRepository);
    }
}
