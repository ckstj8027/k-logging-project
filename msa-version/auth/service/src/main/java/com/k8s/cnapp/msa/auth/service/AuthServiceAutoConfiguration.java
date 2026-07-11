package com.k8s.cnapp.msa.auth.service;

import com.k8s.cnapp.msa.auth.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.auth.infrastructure.policy.PolicyWriter;
import com.k8s.cnapp.msa.auth.infrastructure.tenant.TenantReader;
import com.k8s.cnapp.msa.auth.infrastructure.tenant.TenantWriter;
import com.k8s.cnapp.msa.auth.infrastructure.user.UserReader;
import com.k8s.cnapp.msa.auth.infrastructure.user.UserWriter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AuthServiceAutoConfiguration {

    @Bean
    public CreateDefaultPoliciesUseCase createDefaultPoliciesUseCase(
            PolicyReader policyReader,
            PolicyWriter policyWriter
    ) {
        return new AuthPolicyService(policyReader, policyWriter);
    }

    @Bean
    public SignupUseCase signupUseCase(
            TenantReader tenantReader,
            TenantWriter tenantWriter,
            UserReader userReader,
            UserWriter userWriter,
            CreateDefaultPoliciesUseCase createDefaultPoliciesUseCase
    ) {
        return new SignupService(tenantReader, tenantWriter, userReader, userWriter, createDefaultPoliciesUseCase);
    }

    @Bean
    public LoadUserUseCase loadUserUseCase(UserReader userReader) {
        return new LoadUserService(userReader);
    }
}
