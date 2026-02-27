#!/bin/bash

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# app-network 생성 (이미 있으면 무시)
log "Checking app-network..."
docker network inspect app-network > /dev/null 2>&1 || {
  log "Creating app-network..."
  docker network create app-network
}

# 1. etc (MySQL, MySQL exporter, Nginx exporter)
log "Starting etc containers (MySQL, exporters)..."
docker compose -f "$PROJECT_ROOT/deploy/etc/docker-compose.etc.yml" up -d

# MySQL이 준비될 때까지 대기
log "Waiting for MySQL to be ready..."
until docker exec mysql-db mysqladmin ping -h localhost --silent 2>/dev/null; do
  sleep 2
done
log "MySQL is ready."

# 2. Spring app
log "Starting Spring app container..."
docker compose -f "$PROJECT_ROOT/deploy/spring/docker-compose.spring.yml" up -d

# 3. Nginx
log "Starting Nginx container..."
docker compose -f "$PROJECT_ROOT/deploy/nginx/docker-compose.nginx.yml" up -d

# 4. Monitoring (Prometheus, Grafana)
log "Starting monitoring containers (Prometheus, Grafana)..."
docker compose -f "$PROJECT_ROOT/monitoring/docker-compose.yml" up -d

log "All containers are up!"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
