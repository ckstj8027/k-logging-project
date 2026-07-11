package com.k8s.cnapp.msa.analysis.model;

import java.util.List;

/**
 * K8s SDK 타입이 제거된, 파싱 완료된 스냅샷 (유스케이스 입력 DTO).
 * 각 리스트는 원본 스냅샷에 해당 리소스가 없으면 null 일 수 있다 (기존 동작 보존).
 */
public record ParsedSnapshot(
        List<PodProfile> pods,
        List<NodeProfile> nodes,
        List<ServiceProfile> services,
        List<DeploymentProfile> deployments,
        List<NamespaceProfile> namespaces,
        List<EventProfile> events
) {}
