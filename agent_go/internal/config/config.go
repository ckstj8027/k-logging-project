// Package config 는 환경변수 기반 설정을 로드한다.
// Java agent 와 동일한 환경변수 이름을 사용해 helm 차트를 수정 없이 재사용한다.
package config

import "os"

type Config struct {
	// ingestion 서비스 수집 엔드포인트
	ServerURL string
	// 테넌트 인증용 API Key (X-API-KEY 헤더)
	APIKey string
	// 헬스체크 서버 포트
	Port string
}

func Load() Config {
	return Config{
		ServerURL: getEnv("CNAPP_SERVER_URL", "http://localhost:8080/api/v1/ingestion/raw"),
		APIKey:    getEnv("CNAPP_AGENT_API_KEY", ""),
		Port:      getEnv("AGENT_PORT", "8081"),
	}
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
