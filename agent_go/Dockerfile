# ---- 1단계: 빌드 (Go 툴체인 포함, 이 단계에서 컴파일 검증됨) ----
FROM golang:1.22-alpine AS build
WORKDIR /src
COPY . .
# go mod tidy: 의존성 해석 + go.sum 생성 (네트워크 필요)
RUN go mod tidy && CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o /agent .

# ---- 2단계: 실행 (정적 바이너리만 담은 최소 이미지, ~20MB) ----
FROM gcr.io/distroless/static-debian12:nonroot
COPY --from=build /agent /agent
EXPOSE 8081
ENTRYPOINT ["/agent"]
