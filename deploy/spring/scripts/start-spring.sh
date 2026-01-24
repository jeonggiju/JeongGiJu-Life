#!/bin/bash

cd /home/ubuntu/keep4life/spring

echo "==Start Deploy Spring=="

docker pull 624545858643.dkr.ecr.ap-northeast-2.amazonaws.com/jeonggiju/life:latest

docker compose -f docker-compose.spring.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.spring.yml up -d --build

docker image prune -f

echo "==Complete Deploy Spring=="
