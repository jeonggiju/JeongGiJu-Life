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

log "Building and starting all containers..."
docker compose -f "$PROJECT_ROOT/deploy-oracle/docker-compose.yml" up --build -d

log "All containers are up!"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
