#!/bin/bash

cd /home/ubuntu/jeonggiju-life/spring 2>/dev/null
docker compose -f docker-compose.spring.yml down --remove-orphans 2>/dev/null || true
docker rm -f jeonggiju-life-server 2>/dev/null || true

rm -rf /home/ubuntu/jeonggiju-life/spring