package com.k8s.cnapp.msa.auth.infrastructure.tenant;

import com.k8s.cnapp.msa.auth.model.Tenant;

/**
 * Out-Port: 테넌트 저장 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface TenantWriter {
    /** @return 저장된 테넌트 (생성된 id 포함) */
    Tenant save(Tenant tenant);
}
