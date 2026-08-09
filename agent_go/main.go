// CNAPP Agent (Go + Echo 버전)
//
// 동시성 구조:
//
//	informer 고루틴들 ──▶ chan *ClusterSnapshot (버퍼 100) ──▶ 전송 워커 고루틴 x3
//	     (생산자)              Java BlockingQueue 대응              (소비자)
//
// 종료 순서(graceful shutdown):
//	SIGTERM ▶ informer 중지 대기 ▶ 채널 close ▶ 워커들이 잔여 스냅샷 전송 후 종료
package main

import (
	"context"
	"log/slog"
	"os"
	"os/signal"
	"syscall"

	"github.com/ckstj8027/k-logging-project/agent_go/internal/collector"
	"github.com/ckstj8027/k-logging-project/agent_go/internal/config"
	"github.com/ckstj8027/k-logging-project/agent_go/internal/k8s"
	"github.com/ckstj8027/k-logging-project/agent_go/internal/model"
	"github.com/ckstj8027/k-logging-project/agent_go/internal/sender"
	"github.com/ckstj8027/k-logging-project/agent_go/internal/server"
)

func main() {
	slog.Info("=================================================")
	slog.Info("CNAPP AGENT (Go/Echo) STARTING")
	slog.Info("=================================================")

	cfg := config.Load()

	clientset, err := k8s.NewClient()
	if err != nil {
		slog.Error("failed to create kubernetes client", "error", err)
		os.Exit(1)
	}

	// SIGINT/SIGTERM 을 받으면 ctx 가 취소된다
	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	// Java 의 SnapshotBlockingQueue(용량 100) 에 대응하는 버퍼 채널
	queue := make(chan *model.ClusterSnapshot, 100)

	col := collector.New(clientset, queue)

	// Echo 헬스체크 서버 (/healthz, /readyz)
	server.Start(ctx, cfg.Port, col.Ready)

	// 수집 시작: 최초 전체 스냅샷 → informer 감시
	go func() {
		if err := col.Run(ctx); err != nil {
			slog.Error("collector failed to start", "error", err)
			stop()
		}
	}()

	// 전송 워커 풀 시작 (큐 채널이 닫힐 때까지 소비)
	httpSender := sender.NewHTTPSender(cfg.ServerURL, cfg.APIKey)
	senderDone := make(chan struct{})
	go func() {
		httpSender.Run(ctx, queue)
		close(senderDone)
	}()

	<-ctx.Done()
	slog.Info("shutdown signal received")

	// 1) informer 핸들러 종료 대기 (더 이상 큐에 생산 없음 보장)
	col.Shutdown()
	// 2) 채널을 닫으면 워커들이 잔여 스냅샷을 모두 전송하고 루프를 빠져나온다
	close(queue)
	// 3) 워커 종료 대기
	<-senderDone
	slog.Info("agent stopped gracefully")
}
