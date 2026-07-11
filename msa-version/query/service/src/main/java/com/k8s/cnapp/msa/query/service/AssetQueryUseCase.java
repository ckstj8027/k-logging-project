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
    List<PodProfile> getPods(Long tenantId);

    List<NodeProfile> getNodes(Long tenantId);

    List<ServiceProfile> getServices(Long tenantId);

    List<DeploymentProfile> getDeployments(Long tenantId);

    List<NamespaceProfile> getNamespaces(Long tenantId);

    List<EventProfile> getEvents(Long tenantId);
}
