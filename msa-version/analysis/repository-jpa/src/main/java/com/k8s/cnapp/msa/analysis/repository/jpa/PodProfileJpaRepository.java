package com.k8s.cnapp.msa.analysis.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PodProfileJpaRepository extends JpaRepository<PodProfileEntity, Long> {
    Optional<PodProfileEntity> findByTenantAndAssetContextNamespaceAndAssetContextPodNameAndAssetContextContainerName(
            TenantEntity tenant, String namespace, String podName, String containerName);
}
