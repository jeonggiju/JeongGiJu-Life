#!/bin/bash

cd /home/ubuntu/etc

echo "==Start Deploy DB, Certbot=="

docker compose -f docker-compose.etc.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.etc.yml up -d --build

docker image prune -f

echo "==Complete Deploy DB, Certbot=="
