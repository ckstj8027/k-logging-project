package com.k8s.cnapp.agent.service;

import com.k8s.cnapp.agent.dto.ClusterSnapshot;
import com.k8s.cnapp.agent.queue.SnapshotBlockingQueue;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ClusterSnapshotService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterSnapshotService.class);

    private final SnapshotBlockingQueue queue;
    private final CoreV1Api coreV1Api;
    private final AppsV1Api appsV1Api;
    private final NetworkingV1Api networkingV1Api;

    public ClusterSnapshotService(ApiClient apiClient, SnapshotBlockingQueue queue) {
        this.queue = queue;
        this.coreV1Api = new CoreV1Api(apiClient);
        this.appsV1Api = new AppsV1Api(apiClient);
        this.networkingV1Api = new NetworkingV1Api(apiClient);
    }

    @Scheduled(fixedRateString = "${snapshot.schedule.rate.ms:60000}")
    public void createAndQueueSnapshot() {
        logger.info("Creating cluster snapshot...");
        try {
            // 1. CoreV1Api 리소스 수집
            List<V1Pod> pods = fetchItems(() -> coreV1Api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());
            List<V1Service> services = fetchItems(() -> coreV1Api.listServiceForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());
            List<V1Node> nodes = fetchItems(() -> coreV1Api.listNode(null, null, null, null, null, null, null, null, null, null).getItems());
            List<V1Namespace> namespaces = fetchItems(() -> coreV1Api.listNamespace(null, null, null, null, null, null, null, null, null, null).getItems());
            List<CoreV1Event> events = fetchItems(() -> coreV1Api.listEventForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());

            // 2. AppsV1Api 리소스 수집
            List<V1Deployment> deployments = fetchItems(() -> appsV1Api.listDeploymentForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());
            List<V1StatefulSet> statefulSets = fetchItems(() -> appsV1Api.listStatefulSetForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());
            List<V1DaemonSet> daemonSets = fetchItems(() -> appsV1Api.listDaemonSetForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());
            List<V1ReplicaSet> replicaSets = fetchItems(() -> appsV1Api.listReplicaSetForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());

            // 3. NetworkingV1Api 리소스 수집
            List<V1NetworkPolicy> networkPolicies = fetchItems(() -> networkingV1Api.listNetworkPolicyForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());
            List<V1Ingress> ingresses = fetchItems(() -> networkingV1Api.listIngressForAllNamespaces(null, null, null, null, null, null, null, null, null, null).getItems());

            // 4. DTO 생성 (아래 2번 항목의 Record 필드 순서와 반드시 일치해야 함)
            ClusterSnapshot snapshot = new ClusterSnapshot(
                    pods, services, nodes, namespaces, events,           // Core 리소스
                    deployments, statefulSets, daemonSets, replicaSets,   // Apps 리소스
                    networkPolicies, ingresses                            // Networking 리소스
            );

            queue.put(snapshot);
            logger.info("Successfully created and queued cluster snapshot.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Snapshot creation was interrupted.", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during snapshot creation.", e);
        }
    }

    @FunctionalInterface
    private interface ApiRunner<T> {
        T run() throws ApiException;
    }

    private <T> List<T> fetchItems(ApiRunner<List<T>> runner) {
        try {
            List<T> items = runner.run();
            return items != null ? items : Collections.emptyList();
        } catch (ApiException e) {
            logger.error("K8s API Error: {} - {}", e.getCode(), e.getResponseBody());
        } catch (Exception e) {
            logger.error("Error fetching resources", e);
        }
        return Collections.emptyList();
    }
}