package com.k8s.cnapp.msa.auth.repository.jpa;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EntityScan(basePackageClasses = AuthRepositoryAutoConfiguration.class)
@EnableJpaRepositories(basePackageClasses = AuthRepositoryAutoConfiguration.class)
public class AuthRepositoryAutoConfiguration {

    @Bean
    public TenantJpaAdapter tenantJpaAdapter(TenantJpaRepository tenantJpaRepository) {
        return new TenantJpaAdapter(tenantJpaRepository);
    }

    @Bean
    public UserJpaAdapter userJpaAdapter(UserJpaRepository userJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new UserJpaAdapter(userJpaRepository, tenantJpaRepository);
    }

    @Bean
    public PolicyJpaAdapter policyJpaAdapter(PolicyJpaRepository policyJpaRepository, TenantJpaRepository tenantJpaRepository) {
        return new PolicyJpaAdapter(policyJpaRepository, tenantJpaRepository);
    }
}
