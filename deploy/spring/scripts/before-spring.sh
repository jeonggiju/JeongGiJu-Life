#!/bin/bash

cd /home/ubuntu/spring 2>/dev/null
docker compose -f docker-compose.spring.yml down --remove-orphans 2>/dev/null || true
docker rm -f jeonggiju-life-server 2>/dev/null || true

rm -rf /home/ubuntu/spring