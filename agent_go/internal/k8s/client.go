// Package k8s 는 쿠버네티스 클라이언트를 초기화한다.
// Java agent 의 KubernetesClientConfig 와 동일하게
// in-cluster 설정을 먼저 시도하고, 실패하면 ~/.kube/config 로 폴백한다.
package k8s

import (
	"log/slog"
	"os"
	"path/filepath"

	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
)

func NewClient() (*kubernetes.Clientset, error) {
	cfg, err := rest.InClusterConfig()
	if err != nil {
		slog.Info("in-cluster config unavailable, falling back to kubeconfig")
		home, _ := os.UserHomeDir()
		cfg, err = clientcmd.BuildConfigFromFlags("", filepath.Join(home, ".kube", "config"))
		if err != nil {
			return nil, err
		}
	}
	return kubernetes.NewForConfig(cfg)
}
