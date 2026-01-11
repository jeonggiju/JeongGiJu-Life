#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
docker stop jeonggiju-life-server || true
docker rm jeonggiju-life-server || true
docker pull 624545858643.dkr.ecr.ap-northeast-2.amazonaws.com/jeonggiju/life:latest
docker pull 624545858643.dkr.ecr.ap-northeast-2.amazonaws.com/jeonggiju/nginx:latest

cd /home/ubuntu/jeonggiju-life
docker-compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
echo "--------------- 서버 배포 끝 ------------------"