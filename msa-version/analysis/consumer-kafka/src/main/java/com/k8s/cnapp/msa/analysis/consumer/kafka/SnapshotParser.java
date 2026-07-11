package com.k8s.cnapp.msa.analysis.consumer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k8s.cnapp.msa.analysis.model.AssetContext;
import com.k8s.cnapp.msa.analysis.model.DeploymentProfile;
import com.k8s.cnapp.msa.analysis.model.EventProfile;
import com.k8s.cnapp.msa.analysis.model.NamespaceProfile;
import com.k8s.cnapp.msa.analysis.model.NodeProfile;
import com.k8s.cnapp.msa.analysis.model.ParsedSnapshot;
import com.k8s.cnapp.msa.analysis.model.PodProfile;
import com.k8s.cnapp.msa.analysis.model.ServiceProfile;
import io.kubernetes.client.openapi.models.CoreV1Event;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 기존 AnalysisService 의 V1Pod/V1Node/... → *Profile 매핑 로직을 그대로 추출한 파서.
 * 항목 단위 파싱 실패는 기존과 동일하게 조용히 건너뛴다.
 * 테넌트는 이 시점에 알 수 없으므로 null 로 두고, 유스케이스에서 지정한다.
 */
@RequiredArgsConstructor
class SnapshotParser {

    private final ObjectMapper objectMapper;

    ParsedSnapshot parse(ClusterSnapshot snapshot) {
        return new ParsedSnapshot(
                parsePods(snapshot.pods()),
                parseNodes(snapshot.nodes()),
                parseServices(snapshot.services()),
                parseDeployments(snapshot.deployments()),
                parseNamespaces(snapshot.namespaces()),
                parseEvents(snapshot.events())
        );
    }

    private List<PodProfile> parsePods(List<V1Pod> k8sPods) {
        if (k8sPods == null) return null;
        List<PodProfile> profiles = new ArrayList<>();
        for (V1Pod pod : k8sPods) {
            try {
                String ns = pod.getMetadata().getNamespace();
                String name = pod.getMetadata().getName();
                V1Container container = pod.getSpec().getContainers().get(0);

                Boolean privileged = container.getSecurityContext() != null && Boolean.TRUE.equals(container.getSecurityContext().getPrivileged());
                Long runAsUser = container.getSecurityContext() != null ? container.getSecurityContext().getRunAsUser() : null;
                Boolean allowPrivEsc = container.getSecurityContext() != null ? container.getSecurityContext().getAllowPrivilegeEscalation() : null;
                Boolean readOnlyFs = container.getSecurityContext() != null ? container.getSecurityContext().getReadOnlyRootFilesystem() : null;

                AssetContext assetContext = new AssetContext(ns, name, container.getName(), container.getImage(), null, pod.getStatus().getPhase(), pod.getStatus().getPodIP(), pod.getSpec().getNodeName());

                profiles.add(new PodProfile(null, assetContext, privileged, runAsUser, allowPrivEsc, readOnlyFs, null, null));
            } catch (Exception ignored) {}
        }
        return profiles;
    }

    private List<NodeProfile> parseNodes(List<V1Node> k8sNodes) {
        if (k8sNodes == null) return null;
        List<NodeProfile> profiles = new ArrayList<>();
        for (V1Node node : k8sNodes) {
            try {
                V1NodeStatus s = node.getStatus();
                profiles.add(new NodeProfile(null, node.getMetadata().getName(), s.getNodeInfo().getOsImage(), s.getNodeInfo().getKernelVersion(), s.getNodeInfo().getContainerRuntimeVersion(), s.getNodeInfo().getKubeletVersion(), s.getCapacity().get("cpu").toString(), s.getCapacity().get("memory").toString()));
            } catch (Exception ignored) {}
        }
        return profiles;
    }

    private List<ServiceProfile> parseServices(List<V1Service> k8sServices) {
        if (k8sServices == null) return null;
        List<ServiceProfile> profiles = new ArrayList<>();
        for (V1Service svc : k8sServices) {
            try {
                profiles.add(new ServiceProfile(null, svc.getMetadata().getNamespace(), svc.getMetadata().getName(), svc.getSpec().getType(), svc.getSpec().getClusterIP(), null));
            } catch (Exception ignored) {}
        }
        return profiles;
    }

    private List<DeploymentProfile> parseDeployments(List<V1Deployment> k8sDeploys) {
        if (k8sDeploys == null) return null;
        List<DeploymentProfile> profiles = new ArrayList<>();
        for (V1Deployment d : k8sDeploys) {
            try {
                String selector = objectMapper.writeValueAsString(d.getSpec().getSelector());
                profiles.add(new DeploymentProfile(null, d.getMetadata().getNamespace(), d.getMetadata().getName(), d.getSpec().getReplicas(), d.getStatus().getAvailableReplicas(), d.getSpec().getStrategy().getType(), selector));
            } catch (Exception ignored) {}
        }
        return profiles;
    }

    private List<NamespaceProfile> parseNamespaces(List<V1Namespace> nsList) {
        if (nsList == null) return null;
        List<NamespaceProfile> profiles = new ArrayList<>();
        for (V1Namespace ns : nsList) {
            try {
                profiles.add(new NamespaceProfile(null, ns.getMetadata().getName(), ns.getStatus().getPhase()));
            } catch (Exception ignored) {}
        }
        return profiles;
    }

    private List<EventProfile> parseEvents(List<CoreV1Event> events) {
        if (events == null) return null;
        List<EventProfile> profiles = new ArrayList<>();
        for (CoreV1Event e : events) {
            try {
                profiles.add(new EventProfile(null, e.getMetadata().getNamespace(), e.getInvolvedObject().getKind(), e.getInvolvedObject().getName(), e.getReason(), e.getMessage(), e.getType(), e.getCount(), e.getLastTimestamp(), e.getMetadata().getUid()));
            } catch (Exception ignored) {}
        }
        return profiles;
    }
}
