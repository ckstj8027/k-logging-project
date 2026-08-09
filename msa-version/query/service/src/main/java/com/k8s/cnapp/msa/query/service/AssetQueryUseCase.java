package com.k8s.cnapp.msa.query.service;

import com.k8s.cnapp.msa.query.model.DeploymentProfile;
import com.k8s.cnapp.msa.query.model.EventProfile;
import com.k8s.cnapp.msa.query.model.NamespaceProfile;
import com.k8s.cnapp.msa.query.model.NodeProfile;
import com.k8s.cnapp.msa.query.model.PodProfile;
import com.k8s.cnapp.msa.query.model.ServiceProfile;

import java.util.List;

/**
 * In-Port: 테넌트별 자산(Pod/Node/Service/Deployment/Namespace/Event) 조회 유스케이스.
 */
public interface AssetQueryUseCase {
    List<PodProfile> getPods(Long tenantId, Long lastId, int size);

    List<NodeProfile> getNodes(Long tenantId, Long lastId, int size);

    List<ServiceProfile> getServices(Long tenantId, Long lastId, int size);

    List<DeploymentProfile> getDeployments(Long tenantId, Long lastId, int size);

    List<NamespaceProfile> getNamespaces(Long tenantId, Long lastId, int size);

    List<EventProfile> getEvents(Long tenantId, Long lastId, int size);
}
