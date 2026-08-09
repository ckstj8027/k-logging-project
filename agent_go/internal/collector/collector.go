// Package collector 는 클러스터 리소스를 수집한다.
// Java agent 의 ClusterSnapshotService 역할:
//   - 최초 전체 스냅샷 (11종 리소스 list)
//   - informer 기반 실시간 증분 감시
//   - sanitize + 해시 기반 중복 전송 필터
//   - 삭제 이벤트 전파 (deletedResources)
package collector

import (
	"context"
	"encoding/json"
	"fmt"
	"hash/fnv"
	"log/slog"
	"sync"

	"github.com/ckstj8027/k-logging-project/agent_go/internal/model"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/informers"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/cache"
)

type Collector struct {
	clientset *kubernetes.Clientset
	queue     chan<- *model.ClusterSnapshot
	factory   informers.SharedInformerFactory

	// [중복 이벤트 제거] 리소스 지문(해시) 저장소 — Java 의 ConcurrentHashMap 대응
	mu     sync.Mutex
	hashes map[string]uint64

	synced []cache.InformerSynced
}

func New(clientset *kubernetes.Clientset, queue chan<- *model.ClusterSnapshot) *Collector {
	return &Collector{
		clientset: clientset,
		queue:     queue,
		factory:   informers.NewSharedInformerFactory(clientset, 0), // resync 0 = Java 와 동일
		hashes:    make(map[string]uint64),
	}
}

// Shutdown 은 informer 핸들러가 모두 종료될 때까지 대기한다.
// 이후에 큐 채널을 닫아야 close 된 채널로의 전송(panic)을 막을 수 있다.
func (c *Collector) Shutdown() {
	c.factory.Shutdown()
}

// Ready 는 모든 informer 캐시 동기화 여부를 반환한다 (readiness probe 용).
func (c *Collector) Ready() bool {
	for _, s := range c.synced {
		if !s() {
			return false
		}
	}
	return len(c.synced) > 0
}

// Run 은 최초 전체 스냅샷을 만든 뒤 informer 들을 시작한다.
func (c *Collector) Run(ctx context.Context) error {
	slog.Info("creating initial cluster snapshot...")
	if err := c.initialSnapshot(ctx); err != nil {
		return fmt.Errorf("initial snapshot: %w", err)
	}
	slog.Info("successfully created and queued initial cluster snapshot")

	slog.Info("starting Kubernetes informers for all resource types...")
	c.registerInformers()
	c.factory.Start(ctx.Done())
	cache.WaitForCacheSync(ctx.Done(), c.synced...)
	slog.Info("all informer caches synced")
	return nil
}

// ---------- 최초 전체 스냅샷 ----------

func (c *Collector) initialSnapshot(ctx context.Context) error {
	opts := metav1.ListOptions{}
	pods, err := c.clientset.CoreV1().Pods("").List(ctx, opts)
	if err != nil {
		return err
	}
	services, err := c.clientset.CoreV1().Services("").List(ctx, opts)
	if err != nil {
		return err
	}
	nodes, err := c.clientset.CoreV1().Nodes().List(ctx, opts)
	if err != nil {
		return err
	}
	namespaces, err := c.clientset.CoreV1().Namespaces().List(ctx, opts)
	if err != nil {
		return err
	}
	events, err := c.clientset.CoreV1().Events("").List(ctx, opts)
	if err != nil {
		return err
	}
	deployments, err := c.clientset.AppsV1().Deployments("").List(ctx, opts)
	if err != nil {
		return err
	}
	statefulSets, err := c.clientset.AppsV1().StatefulSets("").List(ctx, opts)
	if err != nil {
		return err
	}
	daemonSets, err := c.clientset.AppsV1().DaemonSets("").List(ctx, opts)
	if err != nil {
		return err
	}
	replicaSets, err := c.clientset.AppsV1().ReplicaSets("").List(ctx, opts)
	if err != nil {
		return err
	}
	networkPolicies, err := c.clientset.NetworkingV1().NetworkPolicies("").List(ctx, opts)
	if err != nil {
		return err
	}
	ingresses, err := c.clientset.NetworkingV1().Ingresses("").List(ctx, opts)
	if err != nil {
		return err
	}

	snapshot := &model.ClusterSnapshot{
		Pods:            sanitizeAll(pods.Items),
		Services:        sanitizeAll(services.Items),
		Nodes:           sanitizeAll(nodes.Items),
		Namespaces:      sanitizeAll(namespaces.Items),
		Events:          sanitizeAll(events.Items),
		Deployments:     sanitizeAll(deployments.Items),
		StatefulSets:    sanitizeAll(statefulSets.Items),
		DaemonSets:      sanitizeAll(daemonSets.Items),
		ReplicaSets:     sanitizeAll(replicaSets.Items),
		NetworkPolicies: sanitizeAll(networkPolicies.Items),
		Ingresses:       sanitizeAll(ingresses.Items),
	}
	// 종료 신호가 이미 온 경우 닫힌 채널로의 전송을 피한다
	select {
	case c.queue <- snapshot:
	case <-ctx.Done():
	}
	return nil
}

// ---------- sanitize (Java 의 sanitize 와 동일 규칙) ----------

func sanitize(meta metav1.Object) {
	meta.SetManagedFields(nil)
	// ResourceVersion 은 의미 없는 상태 업데이트에도 계속 증가하므로 해시 비교에서 제외
	meta.SetResourceVersion("")
	ann := meta.GetAnnotations()
	if ann != nil {
		delete(ann, "kubectl.kubernetes.io/last-applied-configuration")
		if len(ann) == 0 {
			meta.SetAnnotations(nil)
		} else {
			meta.SetAnnotations(ann)
		}
	}
}

// sanitizeAll 은 리스트 항목 전체를 정리해 반환한다 (제네릭).
func sanitizeAll[T any, PT interface {
	*T
	metav1.Object
}](items []T) []T {
	for i := range items {
		sanitize(PT(&items[i]))
	}
	return items
}

// ---------- 증분 업데이트 (informer 콜백) ----------

// handleUpdate 는 변경 이벤트를 중복 필터 후 단건 스냅샷으로 큐에 넣는다.
func (c *Collector) handleUpdate(resourceType string, meta metav1.Object, build func() *model.ClusterSnapshot) {
	defer func() {
		if r := recover(); r != nil {
			slog.Error("error handling incremental update", "type", resourceType, "panic", r)
		}
	}()

	sanitize(meta)

	// [핵심 로직] 중복 전송 방지 필터링 — 객체 JSON 해시가 이전과 같으면 무시
	objectKey := resourceType + "/" + meta.GetNamespace() + "/" + meta.GetName()
	raw, err := json.Marshal(meta)
	if err != nil {
		slog.Error("error hashing resource", "type", resourceType, "error", err)
		return
	}
	h := fnv.New64a()
	h.Write(raw)
	currentHash := h.Sum64()

	c.mu.Lock()
	prev, exists := c.hashes[objectKey]
	if exists && prev == currentHash {
		c.mu.Unlock()
		return // 내용이 동일하면 전송 생략 (로그 플러딩 방지)
	}
	c.hashes[objectKey] = currentHash
	c.mu.Unlock()

	snapshot := build()
	slog.Debug("detected meaningful change, forwarding to server", "key", objectKey)
	c.queue <- snapshot
}

// handleDelete 는 삭제 이벤트를 deletedResources 스냅샷으로 전파한다.
func (c *Collector) handleDelete(resourceType, key string) {
	defer func() {
		if r := recover(); r != nil {
			slog.Error("error handling deletion", "type", resourceType, "panic", r)
		}
	}()
	slog.Info("detected deletion event", "type", resourceType, "key", key)
	c.mu.Lock()
	delete(c.hashes, resourceType+"/"+key)
	c.mu.Unlock()
	c.queue <- &model.ClusterSnapshot{
		DeletedResources: map[string][]string{resourceType: {key}},
	}
}
