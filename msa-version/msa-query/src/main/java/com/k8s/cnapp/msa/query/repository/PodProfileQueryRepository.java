package com.k8s.cnapp.msa.query.repository;

import com.k8s.cnapp.msa.query.domain.PodProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PodProfileQueryRepository extends JpaRepository<PodProfile, Long> {
    List<PodProfile> findAllByTenantId(Long tenantId);
}
