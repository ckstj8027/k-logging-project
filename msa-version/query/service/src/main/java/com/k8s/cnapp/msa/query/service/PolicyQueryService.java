package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.infrastructure.policy.PolicyReader;
import com.k8s.cnapp.msa.query.model.Policy;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class PolicyQueryService implements PolicyQueryUseCase {

    private final PolicyReader policyReader;

    @Override
    public List<Policy> getPolicies(Long tenantId) {
        return policyReader.findAllByTenantId(tenantId);
    }
}
