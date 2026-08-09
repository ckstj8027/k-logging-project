package com.k8s.cnapp.agent.service;

import com.k8s.cnapp.agent.dto.ClusterSnapshot;
import com.k8s.cnapp.agent.queue.SnapshotBlockingQueue;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ClusterSnapshotService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClusterSnapshotService.class);

    private final SnapshotBlockingQueue queue;
    private final ApiClient apiClient;
    private final CoreV1Api coreV1Api;
    private final AppsV1Api appsV1Api;
    private final NetworkingV1Api networkingV1Api;
    private final SharedInformerFactory informerFactory;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    // [추가] 중복 이벤트 제거를 위한 지문(Hash) 저장소
    private final Map<String, Integer> resourceHashes = new ConcurrentHashMap<>();
    // 순수 Gson 은 JDK 17 에서 OffsetDateTime 리플렉션 접근이 막혀 실패하므로
    // k8s 클라이언트가 어댑터를 등록해둔 JSON 직렬화기를 사용한다
    private final io.kubernetes.client.openapi.JSON k8sJson = new io.kubernetes.client.openapi.JSON();

    public ClusterSnapshotService(ApiClient apiClient, SnapshotBlockingQueue queue) {
        this.queue = queue;
        this.apiClient = apiClient;
        this.coreV1Api = new CoreV1Api(apiClient);
        this.appsV1Api = new AppsV1Api(apiClient);
        this.networkingV1Api = new NetworkingV1Api(apiClient);
        this.informerFactory = new SharedInformerFactory(apiClient);
    }

    @Override
    public void run(String... args) {
        // 1. 최초 1회 전체 스냅샷 전송
        createAndQueueSnapshot();

        // 2. Watcher(Informer) 시작
        startInformers();
    }

    public void createAndQueueSnapshot() {
        logger.info("Creating initial cluster snapshot...");
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

            // 4. DTO 생성
            ClusterSnapshot snapshot = new ClusterSnapshot(
                    pods, services, nodes, namespaces, events,
                    deployments, statefulSets, daemonSets, replicaSets,
                    networkPolicies, ingresses,
                    Collections.emptyMap()
            );

            queue.put(snapshot);
            logger.info("Successfully created and queued initial cluster snapshot.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Initial snapshot creation was interrupted.", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during initial snapshot creation.", e);
        }
    }

    private void startInformers() {
        logger.info("Starting Kubernetes Informers for all resource types...");

        registerPodInformer();
        registerServiceInformer();
        registerNodeInformer();
        registerNamespaceInformer();
        registerEventInformer();
        registerDeploymentInformer();
        registerStatefulSetInformer();
        registerDaemonSetInformer();
        registerReplicaSetInformer();
        registerNetworkPolicyInformer();
        registerIngressInformer();

        informerFactory.startAllRegisteredInformers();
    }

    private void registerPodInformer() {
        SharedIndexInformer<V1Pod> informer = informerFactory.sharedIndexInformerFor(
                (params) -> coreV1Api.listPodForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1Pod.class, V1PodList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1Pod obj) { handleUpdate("Pod", obj); }
            @Override public void onUpdate(V1Pod oldObj, V1Pod newObj) { handleUpdate("Pod", newObj); }
            @Override public void onDelete(V1Pod obj, boolean deletedFinalStateUnknown) {
                String containerName = (obj.getSpec() != null && !obj.getSpec().getContainers().isEmpty()) ? obj.getSpec().getContainers().get(0).getName() : "";
                handleDelete("Pod", obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName() + "/" + containerName);
            }
        });
    }

    private void registerServiceInformer() {
        SharedIndexInformer<V1Service> informer = informerFactory.sharedIndexInformerFor(
                (params) -> coreV1Api.listServiceForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1Service.class, V1ServiceList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1Service obj) { handleUpdate("Service", obj); }
            @Override public void onUpdate(V1Service oldObj, V1Service newObj) { handleUpdate("Service", newObj); }
            @Override public void onDelete(V1Service obj, boolean deletedFinalStateUnknown) { handleDelete("Service", obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName()); }
        });
    }

    private void registerNodeInformer() {
        SharedIndexInformer<V1Node> informer = informerFactory.sharedIndexInformerFor(
                (params) -> coreV1Api.listNodeCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1Node.class, V1NodeList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1Node obj) { handleUpdate("Node", obj); }
            @Override public void onUpdate(V1Node oldObj, V1Node newObj) { handleUpdate("Node", newObj); }
            @Override public void onDelete(V1Node obj, boolean deletedFinalStateUnknown) { handleDelete("Node", obj.getMetadata().getName()); }
        });
    }

    private void registerNamespaceInformer() {
        SharedIndexInformer<V1Namespace> informer = informerFactory.sharedIndexInformerFor(
                (params) -> coreV1Api.listNamespaceCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1Namespace.class, V1NamespaceList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1Namespace obj) { handleUpdate("Namespace", obj); }
            @Override public void onUpdate(V1Namespace oldObj, V1Namespace newObj) { handleUpdate("Namespace", newObj); }
            @Override public void onDelete(V1Namespace obj, boolean deletedFinalStateUnknown) { handleDelete("Namespace", obj.getMetadata().getName()); }
        });
    }

    private void registerEventInformer() {
        SharedIndexInformer<CoreV1Event> informer = informerFactory.sharedIndexInformerFor(
                (params) -> coreV1Api.listEventForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                CoreV1Event.class, CoreV1EventList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(CoreV1Event obj) { handleUpdate("Event", obj); }
            @Override public void onUpdate(CoreV1Event oldObj, CoreV1Event newObj) { handleUpdate("Event", newObj); }
            @Override public void onDelete(CoreV1Event obj, boolean deletedFinalStateUnknown) { handleDelete("Event", obj.getMetadata().getUid()); }
        });
    }

    private void registerDeploymentInformer() {
        SharedIndexInformer<V1Deployment> informer = informerFactory.sharedIndexInformerFor(
                (params) -> appsV1Api.listDeploymentForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1Deployment.class, V1DeploymentList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1Deployment obj) { handleUpdate("Deployment", obj); }
            @Override public void onUpdate(V1Deployment oldObj, V1Deployment newObj) { handleUpdate("Deployment", newObj); }
            @Override public void onDelete(V1Deployment obj, boolean deletedFinalStateUnknown) { handleDelete("Deployment", obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName()); }
        });
    }

    private void registerStatefulSetInformer() {
        SharedIndexInformer<V1StatefulSet> informer = informerFactory.sharedIndexInformerFor(
                (params) -> appsV1Api.listStatefulSetForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1StatefulSet.class, V1StatefulSetList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1StatefulSet obj) { handleUpdate("StatefulSet", obj); }
            @Override public void onUpdate(V1StatefulSet oldObj, V1StatefulSet newObj) { handleUpdate("StatefulSet", newObj); }
            @Override public void onDelete(V1StatefulSet obj, boolean deletedFinalStateUnknown) { handleDelete("StatefulSet", obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName()); }
        });
    }

    private void registerDaemonSetInformer() {
        SharedIndexInformer<V1DaemonSet> informer = informerFactory.sharedIndexInformerFor(
                (params) -> appsV1Api.listDaemonSetForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1DaemonSet.class, V1DaemonSetList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1DaemonSet obj) { handleUpdate("DaemonSet", obj); }
            @Override public void onUpdate(V1DaemonSet oldObj, V1DaemonSet newObj) { handleUpdate("DaemonSet", newObj); }
            @Override public void onDelete(V1DaemonSet obj, boolean deletedFinalStateUnknown) { handleDelete("DaemonSet", obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName()); }
        });
    }

    private void registerReplicaSetInformer() {
        SharedIndexInformer<V1ReplicaSet> informer = informerFactory.sharedIndexInformerFor(
                (params) -> appsV1Api.listReplicaSetForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1ReplicaSet.class, V1ReplicaSetList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1ReplicaSet obj) { handleUpdate("ReplicaSet", obj); }
            @Override public void onUpdate(V1ReplicaSet oldObj, V1ReplicaSet newObj) { handleUpdate("ReplicaSet", newObj); }
            @Override public void onDelete(V1ReplicaSet obj, boolean deletedFinalStateUnknown) { handleDelete("ReplicaSet", obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName()); }
        });
    }

    private void registerNetworkPolicyInformer() {
        SharedIndexInformer<V1NetworkPolicy> informer = informerFactory.sharedIndexInformerFor(
                (params) -> networkingV1Api.listNetworkPolicyForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1NetworkPolicy.class, V1NetworkPolicyList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1NetworkPolicy obj) { handleUpdate("NetworkPolicy", obj); }
            @Override public void onUpdate(V1NetworkPolicy oldObj, V1NetworkPolicy newObj) { handleUpdate("NetworkPolicy", newObj); }
            @Override public void onDelete(V1NetworkPolicy obj, boolean deletedFinalStateUnknown) { handleDelete("NetworkPolicy", obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName()); }
        });
    }

    private void registerIngressInformer() {
        SharedIndexInformer<V1Ingress> informer = informerFactory.sharedIndexInformerFor(
                (params) -> networkingV1Api.listIngressForAllNamespacesCall(null, null, null, null, null, null, params.resourceVersion, null, params.timeoutSeconds, params.watch, null),
                V1Ingress.class, V1IngressList.class);
        informer.addEventHandler(new ResourceEventHandler<>() {
            @Override public void onAdd(V1Ingress obj) { handleUpdate("Ingress", obj); }
            @Override public void onUpdate(V1Ingress oldObj, V1Ingress newObj) { handleUpdate("Ingress", newObj); }
            @Override public void onDelete(V1Ingress obj, boolean deletedFinalStateUnknown) { handleDelete("Ingress", obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName()); }
        });
    }

    private void handleUpdate(String type, Object obj) {
        try {
            KubernetesObject k8sObj = (KubernetesObject) obj;
            sanitize(k8sObj);
            
            // [핵심 로직] 중복 전송 방지 필터링
            String objectKey = type + "/" + k8sObj.getMetadata().getNamespace() + "/" + k8sObj.getMetadata().getName();
            int currentHash = k8sJson.serialize(k8sObj).hashCode();
            
            if (resourceHashes.containsKey(objectKey) && resourceHashes.get(objectKey) == currentHash) {
                // 내용이 이전과 완전히 동일하면 큐에 넣지 않고 무시합니다 (로그 플러딩 방지)
                return;
            }
            
            // 변경된 경우 해시 업데이트 및 큐 삽입
            resourceHashes.put(objectKey, currentHash);

            ClusterSnapshot snapshot = switch (type) {
                case "Pod" -> createSnapshot(List.of((V1Pod) obj), null, null, null, null, null, null, null, null, null, null);
                case "Service" -> createSnapshot(null, List.of((V1Service) obj), null, null, null, null, null, null, null, null, null);
                case "Node" -> createSnapshot(null, null, List.of((V1Node) obj), null, null, null, null, null, null, null, null);
                case "Namespace" -> createSnapshot(null, null, null, List.of((V1Namespace) obj), null, null, null, null, null, null, null);
                case "Event" -> createSnapshot(null, null, null, null, List.of((CoreV1Event) obj), null, null, null, null, null, null);
                case "Deployment" -> createSnapshot(null, null, null, null, null, List.of((V1Deployment) obj), null, null, null, null, null);
                case "StatefulSet" -> createSnapshot(null, null, null, null, null, null, List.of((V1StatefulSet) obj), null, null, null, null);
                case "DaemonSet" -> createSnapshot(null, null, null, null, null, null, null, List.of((V1DaemonSet) obj), null, null, null);
                case "ReplicaSet" -> createSnapshot(null, null, null, null, null, null, null, null, List.of((V1ReplicaSet) obj), null, null);
                case "NetworkPolicy" -> createSnapshot(null, null, null, null, null, null, null, null, null, List.of((V1NetworkPolicy) obj), null);
                case "Ingress" -> createSnapshot(null, null, null, null, null, null, null, null, null, null, List.of((V1Ingress) obj));
                default -> null;
            };
            if (snapshot != null) {
                logger.debug("Detected meaningful change in {} - forwarding to server.", objectKey);
                queue.put(snapshot);
            }
        } catch (Exception e) {
            logger.error("Error handling incremental update for {}: {}", type, e.getMessage());
        }
    }

    private void handleDelete(String type, String key) {
        try {
            logger.info("Detected deletion event - Type: {}, Key: {}", type, key);
            resourceHashes.remove(type + "/" + key); // 해시 저장소에서 제거
            ClusterSnapshot snapshot = new ClusterSnapshot(
                    null, null, null, null, null, null, null, null, null, null, null,
                    Map.of(type, List.of(key))
            );
            queue.put(snapshot);
        } catch (Exception e) {
            logger.error("Error handling deletion for {}: {}", type, e.getMessage());
        }
    }

    private ClusterSnapshot createSnapshot(List<V1Pod> pods, List<V1Service> services, List<V1Node> nodes, List<V1Namespace> namespaces, List<CoreV1Event> events,
                                           List<V1Deployment> deployments, List<V1StatefulSet> statefulSets, List<V1DaemonSet> daemonSets, List<V1ReplicaSet> replicaSets,
                                           List<V1NetworkPolicy> networkPolicies, List<V1Ingress> ingresses) {
        return new ClusterSnapshot(
                pods, services, nodes, namespaces, events,
                deployments, statefulSets, daemonSets, replicaSets,
                networkPolicies, ingresses,
                Collections.emptyMap()
        );
    }

    @FunctionalInterface
    private interface ApiRunner<T> {
        T run() throws ApiException;
    }

    private <T extends KubernetesObject> List<T> fetchItems(ApiRunner<List<T>> runner) {
        try {
            List<T> items = runner.run();
            if (items != null) {
                items.forEach(this::sanitize);
                return items;
            }
        } catch (ApiException e) {
            logger.error("K8s API Error: {} - {}", e.getCode(), e.getResponseBody());
        } catch (Exception e) {
            logger.error("Error fetching resources", e);
        }
        return Collections.emptyList();
    }

    private void sanitize(KubernetesObject item) {
        if (item.getMetadata() != null) {
            item.getMetadata().setManagedFields(null);
            
            // [중요] ResourceVersion은 아무 의미 없는 상태 업데이트에도 계속 증가하므로, 
            // 해시값 비교에서 제외하기 위해 무조건 null로 날려버립니다.
            item.getMetadata().setResourceVersion(null);
            
            Map<String, String> annotations = item.getMetadata().getAnnotations();
            if (annotations != null) {
                annotations.remove("kubectl.kubernetes.io/last-applied-configuration");
                if (annotations.isEmpty()) {
                    item.getMetadata().setAnnotations(null);
                }
            }
        }
    }
}
