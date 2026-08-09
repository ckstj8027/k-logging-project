// Package sender 는 스냅샷을 ingestion 서비스로 전송한다.
// Java agent 의 HttpDataSender + DataForwarderService 역할.
// 차이점: 전송 스레드 1개 대신 고루틴 워커 풀(Workers)로 병렬 전송한다.
package sender

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/http"
	"sync"
	"time"

	"github.com/ckstj8027/k-logging-project/agent_go/internal/model"
)

const Workers = 3 // 전송 워커 고루틴 수

type HTTPSender struct {
	serverURL string
	apiKey    string
	client    *http.Client
}

func NewHTTPSender(serverURL, apiKey string) *HTTPSender {
	if apiKey == "" {
		slog.Warn("API Key is not configured. Server might reject the request.")
	}
	return &HTTPSender{
		serverURL: serverURL,
		apiKey:    apiKey,
		client:    &http.Client{Timeout: 30 * time.Second},
	}
}

// Run 은 큐(채널)를 소비하는 워커 고루틴들을 띄우고, 채널이 닫힐 때까지 전송한다.
// ctx 취소 후에도 채널에 남은 스냅샷은 모두 비우고 종료한다 (graceful shutdown).
func (s *HTTPSender) Run(ctx context.Context, queue <-chan *model.ClusterSnapshot) {
	var wg sync.WaitGroup
	for i := 0; i < Workers; i++ {
		wg.Add(1)
		go func(id int) {
			defer wg.Done()
			for snapshot := range queue { // 채널이 닫히면 루프 종료
				if err := s.send(snapshot); err != nil {
					slog.Error("failed to send snapshot", "worker", id, "error", err)
				}
			}
		}(i)
	}
	wg.Wait()
	slog.Info("all sender workers stopped")
}

func (s *HTTPSender) send(snapshot *model.ClusterSnapshot) error {
	body, err := json.Marshal(snapshot)
	if err != nil {
		return fmt.Errorf("marshal: %w", err)
	}
	slog.Info("sending snapshot via HTTP", "url", s.serverURL, "bytes", len(body))

	req, err := http.NewRequest(http.MethodPost, s.serverURL, bytes.NewReader(body))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	if s.apiKey != "" {
		req.Header.Set("X-API-KEY", s.apiKey)
	}

	resp, err := s.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	if resp.StatusCode >= 300 {
		return fmt.Errorf("server returned status %d", resp.StatusCode)
	}
	slog.Info("successfully sent snapshot to server")
	return nil
}
