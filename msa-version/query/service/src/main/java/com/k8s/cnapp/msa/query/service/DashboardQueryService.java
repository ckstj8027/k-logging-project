package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.infrastructure.asset.DeploymentProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.EventProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.NamespaceProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.NodeProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.PodProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.ServiceProfileReader;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
class DashboardQueryService implements DashboardQueryUseCase {

    private final PodProfileReader podProfileReader;
    private final NodeProfileReader nodeProfileReader;
    private final ServiceProfileReader serviceProfileReader;
    private final DeploymentProfileReader deploymentProfileReader;
    private final NamespaceProfileReader namespaceProfileReader;
    private final EventProfileReader eventProfileReader;
    private final AlertQueryUseCase alertQueryUseCase;

    @Override
    public Map<String, Object> getSummary(Long tenantId) {
        Map<String, Object> response = new HashMap<>();
        response.put("podCount", podProfileReader.findAllByTenantId(tenantId).size());
        response.put("nodeCount", nodeProfileReader.findAllByTenantId(tenantId).size());
        response.put("serviceCount", serviceProfileReader.findAllByTenantId(tenantId).size());
        response.put("deploymentCount", deploymentProfileReader.findAllByTenantId(tenantId).size());
        response.put("namespaceCount", namespaceProfileReader.findAllByTenantId(tenantId).size());
        response.put("eventCount", eventProfileReader.findAllByTenantId(tenantId).size());
        response.put("alertCount", alertQueryUseCase.getOpenAlerts(tenantId).size());
        return response;
    }
}
