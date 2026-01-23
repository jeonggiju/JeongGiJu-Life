#!/bin/bash

cd /home/ubuntu/jeonggiju-life 2>/dev/null
docker compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker rm -f certbot postgres-db jeonggiju-life-server nginx 2>/dev/null || true

rm -rf /home/ubuntu/jeonggiju-life