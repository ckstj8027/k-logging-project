package com.k8s.cnapp.msa.analysis.repository;

import com.k8s.cnapp.msa.analysis.domain.PodProfile;
import com.k8s.cnapp.msa.analysis.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PodProfileRepository extends JpaRepository<PodProfile, Long> {
    Optional<PodProfile> findByTenantAndAssetContextNamespaceAndAssetContextPodNameAndAssetContextContainerName(
            Tenant tenant, String namespace, String podName, String containerName);
}
