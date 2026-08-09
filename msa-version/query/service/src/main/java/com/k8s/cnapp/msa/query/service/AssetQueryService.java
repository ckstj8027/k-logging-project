package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.infrastructure.asset.DeploymentProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.EventProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.NamespaceProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.NodeProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.PodProfileReader;
import com.k8s.cnapp.msa.query.infrastructure.asset.ServiceProfileReader;
import com.k8s.cnapp.msa.query.model.DeploymentProfile;
import com.k8s.cnapp.msa.query.model.EventProfile;
import com.k8s.cnapp.msa.query.model.NamespaceProfile;
import com.k8s.cnapp.msa.query.model.NodeProfile;
import com.k8s.cnapp.msa.query.model.PodProfile;
import com.k8s.cnapp.msa.query.model.ServiceProfile;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class AssetQueryService implements AssetQueryUseCase {

    private final PodProfileReader podProfileReader;
    private final NodeProfileReader nodeProfileReader;
    private final ServiceProfileReader serviceProfileReader;
    private final DeploymentProfileReader deploymentProfileReader;
    private final NamespaceProfileReader namespaceProfileReader;
    private final EventProfileReader eventProfileReader;

    @Override
    public List<PodProfile> getPods(Long tenantId, Long lastId, int size) {
        return podProfileReader.findPage(tenantId, lastId, size);
    }

    @Override
    public List<NodeProfile> getNodes(Long tenantId, Long lastId, int size) {
        return nodeProfileReader.findPage(tenantId, lastId, size);
    }

    @Override
    public List<ServiceProfile> getServices(Long tenantId, Long lastId, int size) {
        return serviceProfileReader.findPage(tenantId, lastId, size);
    }

    @Override
    public List<DeploymentProfile> getDeployments(Long tenantId, Long lastId, int size) {
        return deploymentProfileReader.findPage(tenantId, lastId, size);
    }

    @Override
    public List<NamespaceProfile> getNamespaces(Long tenantId, Long lastId, int size) {
        return namespaceProfileReader.findPage(tenantId, lastId, size);
    }

    @Override
    public List<EventProfile> getEvents(Long tenantId, Long lastId, int size) {
        return eventProfileReader.findPage(tenantId, lastId, size);
    }
}
