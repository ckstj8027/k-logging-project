package collector

import (
	"github.com/ckstj8027/k-logging-project/agent_go/internal/model"

	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	networkingv1 "k8s.io/api/networking/v1"
	"k8s.io/client-go/tools/cache"
)

// extract 는 informer 삭제 이벤트의 DeletedFinalStateUnknown 래핑을 벗겨 실제 객체를 꺼낸다.
func extract[T any](obj interface{}) (T, bool) {
	if d, ok := obj.(cache.DeletedFinalStateUnknown); ok {
		obj = d.Obj
	}
	t, ok := obj.(T)
	return t, ok
}

// registerInformers 는 Java agent 와 동일한 11종 리소스 informer 를 등록한다.
// 타입 문자열("Pod" 등)과 삭제 키 형식은 Java 구현과 정확히 일치시킨다.
func (c *Collector) registerInformers() {

	// ---- Pod (삭제 키: ns/name/컨테이너0) ----
	podInf := c.factory.Core().V1().Pods().Informer()
	podInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onPod(o) },
		UpdateFunc: func(_, o interface{}) { c.onPod(o) },
		DeleteFunc: func(o interface{}) {
			if p, ok := extract[*corev1.Pod](o); ok {
				container := ""
				if len(p.Spec.Containers) > 0 {
					container = p.Spec.Containers[0].Name
				}
				c.handleDelete("Pod", p.Namespace+"/"+p.Name+"/"+container)
			}
		},
	})
	c.synced = append(c.synced, podInf.HasSynced)

	// ---- Service ----
	svcInf := c.factory.Core().V1().Services().Informer()
	svcInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onService(o) },
		UpdateFunc: func(_, o interface{}) { c.onService(o) },
		DeleteFunc: func(o interface{}) {
			if s, ok := extract[*corev1.Service](o); ok {
				c.handleDelete("Service", s.Namespace+"/"+s.Name)
			}
		},
	})
	c.synced = append(c.synced, svcInf.HasSynced)

	// ---- Node (삭제 키: name) ----
	nodeInf := c.factory.Core().V1().Nodes().Informer()
	nodeInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onNode(o) },
		UpdateFunc: func(_, o interface{}) { c.onNode(o) },
		DeleteFunc: func(o interface{}) {
			if n, ok := extract[*corev1.Node](o); ok {
				c.handleDelete("Node", n.Name)
			}
		},
	})
	c.synced = append(c.synced, nodeInf.HasSynced)

	// ---- Namespace (삭제 키: name) ----
	nsInf := c.factory.Core().V1().Namespaces().Informer()
	nsInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onNamespace(o) },
		UpdateFunc: func(_, o interface{}) { c.onNamespace(o) },
		DeleteFunc: func(o interface{}) {
			if n, ok := extract[*corev1.Namespace](o); ok {
				c.handleDelete("Namespace", n.Name)
			}
		},
	})
	c.synced = append(c.synced, nsInf.HasSynced)

	// ---- Event (삭제 키: uid) ----
	evInf := c.factory.Core().V1().Events().Informer()
	evInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onEvent(o) },
		UpdateFunc: func(_, o interface{}) { c.onEvent(o) },
		DeleteFunc: func(o interface{}) {
			if e, ok := extract[*corev1.Event](o); ok {
				c.handleDelete("Event", string(e.UID))
			}
		},
	})
	c.synced = append(c.synced, evInf.HasSynced)

	// ---- Deployment ----
	depInf := c.factory.Apps().V1().Deployments().Informer()
	depInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onDeployment(o) },
		UpdateFunc: func(_, o interface{}) { c.onDeployment(o) },
		DeleteFunc: func(o interface{}) {
			if d, ok := extract[*appsv1.Deployment](o); ok {
				c.handleDelete("Deployment", d.Namespace+"/"+d.Name)
			}
		},
	})
	c.synced = append(c.synced, depInf.HasSynced)

	// ---- StatefulSet ----
	stsInf := c.factory.Apps().V1().StatefulSets().Informer()
	stsInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onStatefulSet(o) },
		UpdateFunc: func(_, o interface{}) { c.onStatefulSet(o) },
		DeleteFunc: func(o interface{}) {
			if s, ok := extract[*appsv1.StatefulSet](o); ok {
				c.handleDelete("StatefulSet", s.Namespace+"/"+s.Name)
			}
		},
	})
	c.synced = append(c.synced, stsInf.HasSynced)

	// ---- DaemonSet ----
	dsInf := c.factory.Apps().V1().DaemonSets().Informer()
	dsInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onDaemonSet(o) },
		UpdateFunc: func(_, o interface{}) { c.onDaemonSet(o) },
		DeleteFunc: func(o interface{}) {
			if d, ok := extract[*appsv1.DaemonSet](o); ok {
				c.handleDelete("DaemonSet", d.Namespace+"/"+d.Name)
			}
		},
	})
	c.synced = append(c.synced, dsInf.HasSynced)

	// ---- ReplicaSet ----
	rsInf := c.factory.Apps().V1().ReplicaSets().Informer()
	rsInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onReplicaSet(o) },
		UpdateFunc: func(_, o interface{}) { c.onReplicaSet(o) },
		DeleteFunc: func(o interface{}) {
			if r, ok := extract[*appsv1.ReplicaSet](o); ok {
				c.handleDelete("ReplicaSet", r.Namespace+"/"+r.Name)
			}
		},
	})
	c.synced = append(c.synced, rsInf.HasSynced)

	// ---- NetworkPolicy ----
	npInf := c.factory.Networking().V1().NetworkPolicies().Informer()
	npInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onNetworkPolicy(o) },
		UpdateFunc: func(_, o interface{}) { c.onNetworkPolicy(o) },
		DeleteFunc: func(o interface{}) {
			if n, ok := extract[*networkingv1.NetworkPolicy](o); ok {
				c.handleDelete("NetworkPolicy", n.Namespace+"/"+n.Name)
			}
		},
	})
	c.synced = append(c.synced, npInf.HasSynced)

	// ---- Ingress ----
	ingInf := c.factory.Networking().V1().Ingresses().Informer()
	ingInf.AddEventHandler(cache.ResourceEventHandlerFuncs{
		AddFunc:    func(o interface{}) { c.onIngress(o) },
		UpdateFunc: func(_, o interface{}) { c.onIngress(o) },
		DeleteFunc: func(o interface{}) {
			if i, ok := extract[*networkingv1.Ingress](o); ok {
				c.handleDelete("Ingress", i.Namespace+"/"+i.Name)
			}
		},
	})
	c.synced = append(c.synced, ingInf.HasSynced)
}

// ---- 타입별 업데이트 핸들러: 단건 스냅샷 생성 ----

func (c *Collector) onPod(o interface{}) {
	if p, ok := extract[*corev1.Pod](o); ok {
		p = p.DeepCopy() // informer 캐시 원본을 오염시키지 않도록 복사 후 sanitize
		c.handleUpdate("Pod", p, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{Pods: []corev1.Pod{*p}}
		})
	}
}

func (c *Collector) onService(o interface{}) {
	if s, ok := extract[*corev1.Service](o); ok {
		s = s.DeepCopy()
		c.handleUpdate("Service", s, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{Services: []corev1.Service{*s}}
		})
	}
}

func (c *Collector) onNode(o interface{}) {
	if n, ok := extract[*corev1.Node](o); ok {
		n = n.DeepCopy()
		c.handleUpdate("Node", n, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{Nodes: []corev1.Node{*n}}
		})
	}
}

func (c *Collector) onNamespace(o interface{}) {
	if n, ok := extract[*corev1.Namespace](o); ok {
		n = n.DeepCopy()
		c.handleUpdate("Namespace", n, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{Namespaces: []corev1.Namespace{*n}}
		})
	}
}

func (c *Collector) onEvent(o interface{}) {
	if e, ok := extract[*corev1.Event](o); ok {
		e = e.DeepCopy()
		c.handleUpdate("Event", e, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{Events: []corev1.Event{*e}}
		})
	}
}

func (c *Collector) onDeployment(o interface{}) {
	if d, ok := extract[*appsv1.Deployment](o); ok {
		d = d.DeepCopy()
		c.handleUpdate("Deployment", d, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{Deployments: []appsv1.Deployment{*d}}
		})
	}
}

func (c *Collector) onStatefulSet(o interface{}) {
	if s, ok := extract[*appsv1.StatefulSet](o); ok {
		s = s.DeepCopy()
		c.handleUpdate("StatefulSet", s, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{StatefulSets: []appsv1.StatefulSet{*s}}
		})
	}
}

func (c *Collector) onDaemonSet(o interface{}) {
	if d, ok := extract[*appsv1.DaemonSet](o); ok {
		d = d.DeepCopy()
		c.handleUpdate("DaemonSet", d, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{DaemonSets: []appsv1.DaemonSet{*d}}
		})
	}
}

func (c *Collector) onReplicaSet(o interface{}) {
	if r, ok := extract[*appsv1.ReplicaSet](o); ok {
		r = r.DeepCopy()
		c.handleUpdate("ReplicaSet", r, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{ReplicaSets: []appsv1.ReplicaSet{*r}}
		})
	}
}

func (c *Collector) onNetworkPolicy(o interface{}) {
	if n, ok := extract[*networkingv1.NetworkPolicy](o); ok {
		n = n.DeepCopy()
		c.handleUpdate("NetworkPolicy", n, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{NetworkPolicies: []networkingv1.NetworkPolicy{*n}}
		})
	}
}

func (c *Collector) onIngress(o interface{}) {
	if i, ok := extract[*networkingv1.Ingress](o); ok {
		i = i.DeepCopy()
		c.handleUpdate("Ingress", i, func() *model.ClusterSnapshot {
			return &model.ClusterSnapshot{Ingresses: []networkingv1.Ingress{*i}}
		})
	}
}
