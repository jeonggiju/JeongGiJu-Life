#!/bin/bash

cd /home/ubuntu/jeonggiju-life/etc 2>/dev/null
docker compose -f docker-compose.etc.yml down --remove-orphans 2>/dev/null || true
docker rm -f certbot postgres-db 2>/dev/null || true

rm -rf /home/ubuntu/jeonggiju-life/etc