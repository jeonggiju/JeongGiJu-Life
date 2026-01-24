현재 AWS t3.small 인스턴스 내에서 단일 환경으로 운영 중이며, 아래 4개의 컨테이너가 가동되고 있다.
1. Application: Spring Boot
2. Web Server: Nginx
3. Database: PostgreSQL
4. Utility: Certbot

## 목표
사용자 50명 수용(동시 접속 기준), 트래픽 중가 시 서비스 중단(Downtime) 없는 서버 안정성 및 가용성 확보


