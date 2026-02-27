#!/bin/bash

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

error() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >&2
  exit 1
}

# 필수 파일 확인
[[ -f "$PROJECT_ROOT/deploy/spring/.env" ]] || error "Missing: deploy/spring/.env"

# 이전 실행에서 남은 충돌 컨테이너 정리
log "Cleaning up stale containers..."
docker compose -f "$PROJECT_ROOT/deploy-oracle/docker-compose.yml" down --remove-orphans 2>/dev/null || true
# network_mode: host 컨테이너는 compose down에 포함 안 되므로 개별 제거
docker rm -f nginx-exporter 2>/dev/null || true

log "Building and starting all containers..."
docker compose -f "$PROJECT_ROOT/deploy-oracle/docker-compose.yml" up --build -d

log "All containers are up!"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
