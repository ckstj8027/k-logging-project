package com.k8s.cnapp.msa.query.repository.jpa;

import com.k8s.cnapp.msa.common.model.Status;
import com.k8s.cnapp.msa.query.infrastructure.alert.AlertReader;
import com.k8s.cnapp.msa.query.model.Alert;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class AlertReaderJpaAdapter implements AlertReader {

    private final AlertJpaRepository alertJpaRepository;

    @Override
    public List<Alert> findAllByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, Status status) {
        return alertJpaRepository.findAllByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status).stream()
                .map(AlertEntity::toModel)
                .toList();
    }
}
