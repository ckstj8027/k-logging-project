package com.k8s.cnapp.agent.dto;

import io.kubernetes.client.openapi.models.*;
import java.util.List;

public record ClusterSnapshot(
        // 1. Core 리소스
        List<V1Pod> pods,
        List<V1Service> services,
        List<V1Node> nodes,
        List<V1Namespace> namespaces,
        List<CoreV1Event> events,

        // 2. Apps 리소스
        List<V1Deployment> deployments,
        List<V1StatefulSet> statefulSets,
        List<V1DaemonSet> daemonSets,
        List<V1ReplicaSet> replicaSets,

        // 3. Networking 리소스
        List<V1NetworkPolicy> networkPolicies,
        List<V1Ingress> ingresses
) {}