package com.k8s.cnapp.msa.analysis.consumer.kafka;

import io.kubernetes.client.openapi.models.*;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * IngestionRequestMessage.rawData(JSON 문자열)를 필드명 기반으로 역직렬화하는 DTO.
 * 와이어에 FQCN 이 실리지 않으므로(kafka JsonDeserializer 대상은 IngestionRequestMessage),
 * 기존 com.k8s.cnapp.msa.common.dto 에서 이 모듈로 이동했다.
 */
public record ClusterSnapshot(
        List<V1Pod> pods,
        List<V1Service> services,
        List<V1Node> nodes,
        List<V1Namespace> namespaces,
        List<CoreV1Event> events,
        List<V1Deployment> deployments,
        List<V1StatefulSet> statefulSets,
        List<V1DaemonSet> daemonSets,
        List<V1ReplicaSet> replicaSets,
        List<V1NetworkPolicy> networkPolicies,
        List<V1Ingress> ingresses,
        Map<String, List<String>> deletedResources
) implements Serializable {}
