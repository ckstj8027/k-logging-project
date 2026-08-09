// Package model 은 서버(ingestion/analysis)와의 JSON 계약을 정의한다.
// !! 최상위 필드명은 Java agent 의 ClusterSnapshot record 와 동일해야 한다 !!
// (analysis 서비스가 이 필드명으로 역직렬화한다)
package model

import (
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	networkingv1 "k8s.io/api/networking/v1"
)

type ClusterSnapshot struct {
	// 1. Core 리소스
	Pods       []corev1.Pod       `json:"pods,omitempty"`
	Services   []corev1.Service   `json:"services,omitempty"`
	Nodes      []corev1.Node      `json:"nodes,omitempty"`
	Namespaces []corev1.Namespace `json:"namespaces,omitempty"`
	Events     []corev1.Event     `json:"events,omitempty"`

	// 2. Apps 리소스
	Deployments  []appsv1.Deployment  `json:"deployments,omitempty"`
	StatefulSets []appsv1.StatefulSet `json:"statefulSets,omitempty"`
	DaemonSets   []appsv1.DaemonSet   `json:"daemonSets,omitempty"`
	ReplicaSets  []appsv1.ReplicaSet  `json:"replicaSets,omitempty"`

	// 3. Networking 리소스
	NetworkPolicies []networkingv1.NetworkPolicy `json:"networkPolicies,omitempty"`
	Ingresses       []networkingv1.Ingress       `json:"ingresses,omitempty"`

	// 4. 삭제 리소스 (타입 -> 삭제된 리소스 키 목록)
	DeletedResources map[string][]string `json:"deletedResources,omitempty"`
}
