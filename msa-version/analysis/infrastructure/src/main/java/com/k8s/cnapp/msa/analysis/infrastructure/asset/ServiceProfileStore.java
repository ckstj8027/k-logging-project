package com.k8s.cnapp.msa.analysis.infrastructure.asset;

import com.k8s.cnapp.msa.analysis.model.ServiceProfile;
import com.k8s.cnapp.msa.analysis.model.Tenant;

import java.util.Optional;

/**
 * Out-Port: Service 프로파일 영속성 규격. 구현은 repository-jpa 어댑터가 제공한다.
 */
public interface ServiceProfileStore {

    Optional<ServiceProfile> findByTenantAndNamespaceAndName(Tenant tenant, String namespace, String name);

    ServiceProfile save(ServiceProfile profile);
}
