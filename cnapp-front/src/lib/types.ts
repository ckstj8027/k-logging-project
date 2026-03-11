// 공통 페이징 요청 파라미터
export interface PagingParams {
    lastId?: number;
    size?: number;
}

export interface AlertDto {
    id: number;
    severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
    category: 'RUNTIME' | 'CSPM';
    message: string;
    resourceType: string;
    resourceName: string;
    status: 'OPEN' | 'RESOLVED';
    createdAt: string;
}

export interface PolicyDto {
    id: number;
    resourceType: string;
    ruleType: string;
    value: string;
    enabled: boolean;
    description: string;
}

// 자산(Assets) - Pods
export interface PodDto {
    id: number;
    namespace: string;
    podName: string;
    status: string;
    podIp: string | null;
    nodeName: string;
    containerName: string;
    image: string;
    privileged: boolean;
    runAsRoot: boolean;
    deploymentName: string | null;
    createdAt: string;
}

// 자산(Assets) - Services
export interface ServiceDto {
    id: number;
    name: string;
    namespace: string;
    type: string;
    clusterIp: string;
    externalIps: string[] | null;
    createdAt: string;
}

// 자산(Assets) - Deployments
export interface DeploymentDto {
    id: number;
    name: string;
    namespace: string;
    replicas: number;
    availableReplicas: number;
    strategyType: string;
    createdAt: string;
}

// 자산(Assets) - Namespaces
export interface NamespaceDto {
    id: number;
    name: string;
    status: string;
    createdAt: string;
}

// 자산(Assets) - Nodes
export interface NodeDto {
    id: number;
    name: string;
    osImage: string;
    kernelVersion: string;
    containerRuntimeVersion: string;
    kubeletVersion: string;
    createdAt: string;
}

// 자산(Assets) - Events
export interface EventDto {
    id: number;
    name: string;
    namespace: string;
    involvedObjectKind: string;
    involvedObjectName: string;
    reason: string;
    message: string;
    type: string;
    lastTimestamp: string;
    createdAt: string;
}

export interface DashboardSummary {
    podCount: number;
    serviceCount: number;
    nodeCount: number;
    namespaceCount: number;
    deploymentCount: number;
    eventCount: number;
    alertCount: number;
}
