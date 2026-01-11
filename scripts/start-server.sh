#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
docker stop jeonggiju-life-server || true
docker rm jeonggiju-life-server || true

cd /home/ubuntu/jeonggiju-life
docker-compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

echo "--------------- 서버 배포 끝 ------------------"