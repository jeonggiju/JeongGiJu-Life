#!/bin/bash

cd /home/ubuntu/keep4life/nginx 2>/dev/null
docker compose -f docker-compose.nginx.yml down --remove-orphans 2>/dev/null || true
docker rm -f nginx-server 2>/dev/null || true

rm -rf /home/ubuntu/keep4life/nginx