package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.model.Policy;

import java.util.List;

/**
 * In-Port: 테넌트별 정책 조회 유스케이스.
 */
public interface PolicyQueryUseCase {
    List<Policy> getPolicies(Long tenantId);
}
