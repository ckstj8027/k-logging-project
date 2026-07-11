package com.k8s.cnapp.msa.analysis.infrastructure.policy;

import com.k8s.cnapp.msa.analysis.model.Policy;
import com.k8s.cnapp.msa.analysis.model.Tenant;

import java.util.List;

/**
 * Out-Port: 테넌트별 정책 설정 조회 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface PolicyReader {

    List<Policy> findAllByTenant(Tenant tenant);
}
