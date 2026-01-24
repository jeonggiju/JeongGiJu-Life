#!/bin/bash

cd /home/ubuntu/keep4life/nginx

echo "==Start Deploy Nginx=="

docker pull 624545858643.dkr.ecr.ap-northeast-2.amazonaws.com/jeonggiju/nginx:latest

docker compose -f docker-compose.nginx.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.nginx.yml up -d --build

docker image prune -f

echo "==Complete Deploy Nginx=="
