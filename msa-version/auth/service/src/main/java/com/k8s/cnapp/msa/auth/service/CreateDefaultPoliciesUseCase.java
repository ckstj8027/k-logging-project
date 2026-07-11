package com.k8s.cnapp.msa.auth.service;

import com.k8s.cnapp.msa.auth.model.Tenant;

/**
 * In-Port: 테넌트 기본 정책(11개) 생성 유스케이스. 기존 AuthPolicyService 로직을 그대로 보존한다.
 */
public interface CreateDefaultPoliciesUseCase {
    void createDefaultPoliciesForTenant(Tenant tenant);
}
