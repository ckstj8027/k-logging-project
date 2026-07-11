package com.k8s.cnapp.msa.analysis.repository.jpa;

import com.k8s.cnapp.msa.analysis.infrastructure.alert.AlertWriter;
import com.k8s.cnapp.msa.analysis.model.Alert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class AlertWriterJpaAdapter implements AlertWriter {

    private final AlertJpaRepository alertJpaRepository;
    private final TenantJpaRepository tenantJpaRepository;

    @Override
    public void save(Alert alert) {
        alertJpaRepository.save(new AlertEntity(
                tenantJpaRepository.getReferenceById(alert.getTenant().getId()),
                alert.getSeverity(),
                alert.getCategory(),
                alert.getMessage(),
                alert.getResourceType(),
                alert.getResourceName()
        ));
    }
}
