#!/bin/bash

cd /home/ubuntu/nginx 2>/dev/null
docker compose -f docker-compose.nginx.yml down --remove-orphans 2>/dev/null || true
docker rm -f nginx 2>/dev/null || true

rm -rf /home/ubuntu/nginx