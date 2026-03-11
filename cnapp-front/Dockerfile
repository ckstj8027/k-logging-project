# --- Stage 1: Build Stage ---
# 빌드는 이미 로컬에서 성공했으므로, 도커 내에서도 동일한 환경을 보장하기 위해 Node 20을 사용합니다.
FROM node:20-alpine AS builder

WORKDIR /app

# 의존성 설치
COPY package*.json ./
RUN npm install

# 소스 코드 복사
COPY . .

# SvelteKit 정적 빌드 실행 (위에서 성공한 그 과정을 도커 안에서도 수행합니다)
RUN npx svelte-kit sync
RUN npm run build

# --- Stage 2: Production Stage ---
# 실제 서비스는 가벼운 Nginx를 사용합니다.
FROM nginx:stable-alpine

# 기존 Nginx 기본 파일들을 삭제하여 충돌을 방지합니다.
RUN rm -rf /usr/share/nginx/html/*
RUN rm -rf /etc/nginx/conf.d/*

# [중요] svelte.config.js에서 설정한 'build' 폴더의 결과물을 Nginx 경로로 복사합니다.
COPY --from=builder /app/build /usr/share/nginx/html

# SPA 라우팅을 지원하는 커스텀 Nginx 설정 적용
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]