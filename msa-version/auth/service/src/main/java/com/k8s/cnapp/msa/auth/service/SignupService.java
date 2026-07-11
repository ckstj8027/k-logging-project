package com.k8s.cnapp.msa.auth.service;

import com.k8s.cnapp.msa.auth.exception.DuplicateCompanyNameException;
import com.k8s.cnapp.msa.auth.exception.DuplicateUsernameException;
import com.k8s.cnapp.msa.auth.infrastructure.tenant.TenantReader;
import com.k8s.cnapp.msa.auth.infrastructure.tenant.TenantWriter;
import com.k8s.cnapp.msa.auth.infrastructure.user.UserReader;
import com.k8s.cnapp.msa.auth.infrastructure.user.UserWriter;
import com.k8s.cnapp.msa.auth.model.Tenant;
import com.k8s.cnapp.msa.auth.model.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class SignupService implements SignupUseCase {

    private final TenantReader tenantReader;
    private final TenantWriter tenantWriter;
    private final UserReader userReader;
    private final UserWriter userWriter;
    private final CreateDefaultPoliciesUseCase createDefaultPoliciesUseCase;

    @Override
    public String signup(String companyName, String username, String password) {
        if (tenantReader.findByName(companyName).isPresent()) {
            throw new DuplicateCompanyNameException();
        }

        if (userReader.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException();
        }

        Tenant tenant = tenantWriter.save(Tenant.create(companyName));

        createDefaultPoliciesUseCase.createDefaultPoliciesForTenant(tenant);

        userWriter.save(User.create(username, "{noop}" + password, User.Role.ADMIN, tenant.id()));

        return tenant.apiKey();
    }
}
