// Package server 는 Echo 로 헬스체크 엔드포인트를 제공한다.
// (k8s liveness/readiness probe 용 — agent 의 유일한 HTTP 서버 역할)
package server

import (
	"context"
	"log/slog"
	"net/http"

	"github.com/labstack/echo/v4"
)

func Start(ctx context.Context, port string, ready func() bool) {
	e := echo.New()
	e.HideBanner = true

	e.GET("/healthz", func(c echo.Context) error {
		return c.String(http.StatusOK, "ok")
	})
	e.GET("/readyz", func(c echo.Context) error {
		if ready() {
			return c.String(http.StatusOK, "ready")
		}
		return c.String(http.StatusServiceUnavailable, "not ready")
	})

	go func() {
		<-ctx.Done()
		_ = e.Shutdown(context.Background())
	}()
	go func() {
		if err := e.Start(":" + port); err != nil && err != http.ErrServerClosed {
			slog.Error("health server stopped", "error", err)
		}
	}()
}
