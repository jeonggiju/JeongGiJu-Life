#!/bin/bash

# keep4.life HTTPS 초기 인증서 발급 스크립트
# 이 스크립트는 EC2에서 단 한 번만 실행하면 됩니다.

set -e

echo "================================================"
echo "  keep4.life HTTPS 인증서 초기 발급"
echo "================================================"
echo ""

domains=(keep4.life www.keep4.life)
rsa_key_size=4096
data_path="/home/ubuntu/certbot"
email="halogiju123@gmail.com"  # ⚠️ 이메일 주소를 입력하세요
staging=0  # 테스트시 1, 실제 발급시 0

# 이메일 확인
if [ -z "$email" ]; then
  echo "⚠️  경고: 이메일 주소를 입력해주세요!"
  read -p "이메일 주소 입력: " email
  if [ -z "$email" ]; then
    echo "❌ 이메일은 필수입니다."
    exit 1
  fi
fi

# 기존 인증서 확인
if [ -d "$data_path/conf/live/${domains[0]}" ]; then
  echo "⚠️  기존 인증서가 존재합니다."
  read -p "삭제하고 새로 발급하시겠습니까? (y/N) " decision
  if [ "$decision" != "Y" ] && [ "$decision" != "y" ]; then
    echo "종료합니다."
    exit 0
  fi
  echo "기존 인증서 삭제 중..."
  sudo rm -rf "$data_path/conf/live/${domains[0]}"
  sudo rm -rf "$data_path/conf/archive/${domains[0]}"
  sudo rm -rf "$data_path/conf/renewal/${domains[0]}.conf"
fi

# 디렉토리 생성
echo "1. 필요한 디렉토리 생성 중..."
sudo mkdir -p "$data_path/www"
sudo mkdir -p "$data_path/conf/live/${domains[0]}"
echo "   ✓ 디렉토리 생성 완료"
echo ""

# 더미 인증서 생성 (Nginx 초기 시작용)
echo "2. 더미 인증서 생성 중..."
path="/etc/letsencrypt/live/${domains[0]}"
sudo docker run --rm -v "$data_path/conf:/etc/letsencrypt" \
  certbot/certbot \
  sh -c "openssl req -x509 -nodes -newkey rsa:$rsa_key_size -days 1 \
    -keyout '$path/privkey.pem' \
    -out '$path/fullchain.pem' \
    -subj '/CN=localhost'"
echo "   ✓ 더미 인증서 생성 완료"
echo ""

# 현재 디렉토리로 이동 (docker-compose.prod.yml이 있는 곳)
cd /home/ubuntu/jeonggiju-life

# Nginx 시작
echo "3. Nginx 컨테이너 시작 중..."
sudo docker compose -f docker-compose.prod.yml up -d nginx
sleep 5
echo "   ✓ Nginx 시작 완료"
echo ""

# 더미 인증서 삭제
echo "4. 더미 인증서 삭제 중..."
sudo docker run --rm -v "$data_path/conf:/etc/letsencrypt" \
  certbot/certbot \
  sh -c "rm -rf /etc/letsencrypt/live/${domains[0]} && \
         rm -rf /etc/letsencrypt/archive/${domains[0]} && \
         rm -rf /etc/letsencrypt/renewal/${domains[0]}.conf"
echo "   ✓ 더미 인증서 삭제 완료"
echo ""

# Let's Encrypt 인증서 발급
echo "5. Let's Encrypt 실제 인증서 발급 중..."
domain_args=""
for domain in "${domains[@]}"; do
  domain_args="$domain_args -d $domain"
done

case "$staging" in
  "1") staging_arg="--staging" ;;
  *) staging_arg="" ;;
esac

sudo docker run --rm \
  -v "$data_path/conf:/etc/letsencrypt" \
  -v "$data_path/www:/var/www/certbot" \
  certbot/certbot \
  certonly --webroot -w /var/www/certbot \
    $staging_arg \
    $domain_args \
    --email $email \
    --rsa-key-size $rsa_key_size \
    --agree-tos \
    --no-eff-email \
    --force-renewal

echo ""
echo "   ✓ 인증서 발급 완료"
echo ""

# Nginx 재시작
echo "6. Nginx 재로드 중..."
sudo docker exec nginx-server nginx -s reload
echo "   ✓ Nginx 재로드 완료"
echo ""

# Certbot 컨테이너 시작 (자동 갱신용)
echo "7. Certbot 자동 갱신 컨테이너 시작 중..."
sudo docker compose -f docker-compose.prod.yml up -d certbot
echo "   ✓ Certbot 컨테이너 시작 완료"
echo ""

echo "================================================"
echo "  ✅ HTTPS 인증서 초기 발급 완료!"
echo "================================================"
echo ""
echo "인증서 확인:"
echo "  sudo ls -la $data_path/conf/live/keep4.life/"
echo ""
echo "브라우저 접속:"
echo "  https://keep4.life"
echo ""
echo "이제 GitHub에 코드를 push하면 자동으로 배포됩니다."
echo "인증서는 자동으로 갱신되므로 추가 작업이 필요 없습니다."
echo ""