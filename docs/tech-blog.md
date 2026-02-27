# Keep4Life 프로젝트 아키텍처

## 프로젝트 개요

Keep4Life는 일상을 기록하고 친구와 공유하는 웹 서비스입니다. 6가지 기록 방식(체크/텍스트/시간/숫자/체크리스트/가계부)을 지원하며, 좋아요/댓글/1:1 채팅 등 소셜 기능과 SSE 기반 실시간 알림을 제공합니다.

> 서비스 주소: https://keep4.life

> [스크린샷: 서비스 메인 화면]

---

## 1. 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 3.5.9 |
| **Security** | Spring Security + JWT | nimbus-jose-jwt 10.3 |
| **Database** | PostgreSQL | 16 |
| **ORM** | Spring Data JPA + Hibernate | - |
| **Query** | QueryDSL | 5.1.0 (Jakarta) |
| **API 문서** | Springdoc OpenAPI | 2.7.0 |
| **스토리지** | AWS S3 | SDK 2.31.7 |
| **모니터링** | Prometheus + Grafana | Micrometer |
| **프론트엔드** | Vanilla JS + SSE | - |
| **웹 서버** | Nginx (리버스 프록시, HTTPS) | - |
| **컨테이너** | Docker + Docker Compose | 3.8 |
| **CI/CD** | GitHub Actions + AWS CodeDeploy | - |
| **인프라** | AWS (EC2, ECR, S3, CodeDeploy) | ap-northeast-2 |
| **SSL** | Let's Encrypt (Certbot) | - |
| **UUID** | uuid-creator | 6.1.1 |

> [스크린샷: build.gradle 의존성 목록]

---

## 2. ERD (Entity Relationship Diagram)

### 2-1. 전체 엔티티 관계도

```
User (UUID PK)
 ├── Category (1:N)
 │    ├── CheckRecord      (N:1, category_id+date UNIQUE)
 │    ├── TextRecord        (N:1)
 │    │    └── TextRecordImage (1:N, displayOrder 정렬)
 │    ├── TimeRecord        (N:1, category_id+date UNIQUE)
 │    ├── NumberRecord      (N:1, category_id+date UNIQUE)
 │    ├── CheckListRecord   (N:1)
 │    ├── ExpenseRecord     (N:1)
 │    │    └── ExpenseRecordTag (M:N 조인 테이블)
 │    ├── Comment           (N:1, parent_id self-referencing)
 │    └── CategoryLike      (N:1, user_id+category_id UNIQUE)
 ├── ExpenseTag (1:N, 사용자별 태그)
 ├── Friendship (requester_id/receiver_id, UNIQUE)
 ├── ChatRoom (user1_id/user2_id, UNIQUE)
 │    └── ChatMessage (N:1)
 └── Notification (sender_id/receiver_id, JSONB data)
```

> [스크린샷: ERD 다이어그램 이미지]

### 2-2. User

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | UUID | PK | |
| email | VARCHAR | UNIQUE, NOT NULL | 로그인 이메일 |
| username | VARCHAR | | 사용자명 |
| nickname | VARCHAR | | 닉네임 |
| password | TEXT | | 비밀번호 (prod: BCrypt) |
| profile_image_url | VARCHAR | | 프로필 이미지 URL (S3) |
| title | VARCHAR | | 프로필 타이틀 |
| description | TEXT | | 프로필 소개글 |
| authority | VARCHAR | | ROLE_USER, ROLE_ADMIN |
| birth_year | INT | | |
| birth_month | INT | | |
| birth_day | INT | | |

### 2-3. Category

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|---------|------|
| id | UUID | PK | |
| user_id | UUID | FK → users | 소유자 |
| title | VARCHAR | | 카테고리 제목 |
| description | TEXT | | 카테고리 설명 |
| record_type | VARCHAR | NOT NULL | CHECK, TEXT, TIME, NUMBER, CHECKLIST, EXPENSE |
| visibility | VARCHAR | NOT NULL | PUBLIC, PRIVATE |
| created_at | TIMESTAMP | NOT NULL | 생성 시간 |

### 2-4. 기록 테이블 (6종)

**CheckRecord** - 성공/실패 기록

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| category_id | UUID | FK, UNIQUE(category_id, date) |
| success | BOOLEAN | |
| date | DATE | |

**TextRecord** - 텍스트+이미지 기록

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| category_id | UUID | FK, NOT NULL |
| title | VARCHAR | |
| text | TEXT | |
| date | DATE | |

**TextRecordImage** - 텍스트 기록의 이미지

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| text_record_id | UUID | FK, NOT NULL |
| image_url | VARCHAR | S3 URL |
| display_order | INT | 정렬 순서 |

**TimeRecord** - 시간 기록

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| category_id | UUID | FK, UNIQUE(category_id, date) |
| time | TIME | |
| date | DATE | |

**NumberRecord** - 숫자 기록

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| category_id | UUID | FK, UNIQUE(category_id, date) |
| number | DOUBLE | |
| date | DATE | |

**CheckListRecord** - 체크리스트 기록

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| category_id | UUID | FK |
| todo | VARCHAR | 할 일 내용 |
| success | BOOLEAN | 완료 여부 |
| date | DATE | |

**ExpenseRecord** - 가계부 기록

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| category_id | UUID | FK |
| amount | DOUBLE | 금액 |
| expense_type | VARCHAR | INCOME, EXPENSE |
| payment_method | VARCHAR | CASH, CARD, TRANSFER, OTHER |
| merchant | VARCHAR | 가맹점 |
| memo | VARCHAR | 메모 |
| date | DATE | |

### 2-5. 태그 시스템

**ExpenseTag** - 사용자별 지출 태그

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| user_id | UUID | FK → users |
| name | VARCHAR | 태그명 |

**ExpenseRecordTag** - 기록-태그 연결 (M:N 조인 테이블)

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| expense_record_id | UUID | FK → expense_record |
| expense_tag_id | UUID | FK → expense_tag |

### 2-6. 소셜 테이블

**CategoryLike** - 좋아요

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| user_id | UUID | FK, UNIQUE(user_id, category_id) |
| category_id | UUID | FK |
| created_at | TIMESTAMP | |

**Comment** - 댓글 (self-referencing 답글)

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| comment | VARCHAR | 댓글 내용 |
| parent_id | UUID | FK → comment (null이면 최상위) |
| category_id | UUID | FK |
| user_id | UUID | FK |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | 수정 시 자동 갱신 |

**Friendship** - 친구 관계

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| requester_id | UUID | FK, UNIQUE(requester_id, receiver_id) |
| receiver_id | UUID | FK |
| status | VARCHAR | PENDING, ACCEPTED, REJECTED, BLOCKED |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | |

**ChatRoom** - 채팅방

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| user1_id | UUID | FK, UNIQUE(user1_id, user2_id) |
| user2_id | UUID | FK |
| created_at | TIMESTAMP | NOT NULL |
| last_message_at | TIMESTAMP | 마지막 메시지 시간 |

**ChatMessage** - 채팅 메시지

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK |
| chat_room_id | UUID | FK, NOT NULL |
| sender_id | UUID | FK, NOT NULL |
| content | TEXT | NOT NULL |
| is_read | BOOLEAN | NOT NULL, default false |
| created_at | TIMESTAMP | NOT NULL |

### 2-7. 알림

**Notification**

| 컬럼 | 타입 | 제약조건 |
|------|------|---------|
| id | UUID | PK (time-ordered epoch UUID) |
| receiver_id | UUID | FK, NOT NULL |
| sender_id | UUID | FK, NOT NULL |
| type | VARCHAR | NOT NULL (COMMENT, LIKE, REPLY, FRIEND_REQUEST, FRIEND_ACCEPT, CHAT_MESSAGE) |
| data | JSONB | 알림 유형별 유연한 페이로드 |
| read | BOOLEAN | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

### 2-8. Enum 정리

| Enum | 값 |
|------|-----|
| RecordType | `CHECK`, `TEXT`, `TIME`, `NUMBER`, `CHECKLIST`, `EXPENSE` |
| Visibility | `PUBLIC`, `PRIVATE` |
| Authority | `ROLE_USER`, `ROLE_ADMIN` |
| FriendshipStatus | `PENDING`, `ACCEPTED`, `REJECTED`, `BLOCKED` |
| NotificationType | `COMMENT`, `LIKE`, `REPLY`, `FRIEND_REQUEST`, `FRIEND_ACCEPT`, `CHAT_MESSAGE` |
| ExpenseType | `INCOME`, `EXPENSE` |
| PaymentMethod | `CASH`, `CARD`, `TRANSFER`, `OTHER` |
| TokenType | `ACCESS("access")`, `REFRESH("refresh")` |

---

## 3. 클라우드 아키텍처

### 3-1. 전체 인프라 구성

```
[사용자 브라우저]
       │
       ▼
[Route 53 / DNS: keep4.life]
       │
       ▼
[EC2 인스턴스 (ap-northeast-2)]
  ┌────┴────────────────────────────────────┐
  │  Docker Network: app-network            │
  │                                         │
  │  ┌─────────────┐    ┌────────────────┐  │
  │  │   Nginx     │───▶│  Spring Boot   │  │
  │  │  :80/:443   │    │  :8080/:8081   │  │
  │  └─────────────┘    └────────────────┘  │
  │         │                    │           │
  │         │            ┌──────┴──────┐    │
  │         │            │ PostgreSQL  │    │
  │         │            │    :5432    │    │
  │         │            └─────────────┘    │
  │         │                               │
  │  ┌──────┴──────┐  ┌────────────────┐   │
  │  │   Certbot   │  │  Prometheus    │   │
  │  │ Let's Encrypt│  │    :9090      │   │
  │  └─────────────┘  └───────┬────────┘   │
  │                           │             │
  │                    ┌──────┴──────┐      │
  │                    │   Grafana   │      │
  │                    │    :3000    │      │
  │                    └─────────────┘      │
  │                                         │
  │  ┌───────────────┐ ┌───────────────┐   │
  │  │nginx-exporter │ │postgres-export│   │
  │  │    :9113      │ │    :9187      │   │
  │  └───────────────┘ └───────────────┘   │
  └─────────────────────────────────────────┘
                    │
                    ▼
             [AWS S3 Bucket]
          프로필 이미지, 기록 이미지
```

> [스크린샷: AWS 콘솔 - EC2 인스턴스 또는 아키텍처 다이어그램]

### 3-2. Docker Compose 구성

서비스를 3개의 Docker Compose로 분리하여 독립적으로 배포합니다.

**docker-compose.spring.yml** - Spring Boot 애플리케이션

```yaml
services:
  app:
    image: <ECR_REGISTRY>/jeonggiju/life:latest
    container_name: jeonggiju-life-server
    restart: unless-stopped
    env_file: .env
    ports:
      - "8081:8081"  # Actuator (모니터링)
    networks:
      - app-network
```

**docker-compose.nginx.yml** - Nginx 리버스 프록시

```yaml
services:
  nginx:
    image: <ECR_REGISTRY>/jeonggiju/nginx:latest
    container_name: nginx-server
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /home/ubuntu/certbot/conf:/etc/letsencrypt:ro
      - /home/ubuntu/certbot/www:/var/www/certbot:ro
    networks:
      - app-network
```

**docker-compose.etc.yml** - DB, Certbot, Exporter

```yaml
services:
  db:
    image: postgres:16
    container_name: postgres-db
    environment:
      POSTGRES_DB: app
      POSTGRES_USER: ${SPRING_DATASOURCE_USERNAME}
      POSTGRES_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks:
      - app-network

  certbot:
    image: certbot/certbot
    volumes:
      - /home/ubuntu/certbot/conf:/etc/letsencrypt
      - /home/ubuntu/certbot/www:/var/www/certbot

  postgres-exporter:
    image: prometheuscommunity/postgres-exporter
    ports:
      - "9187:9187"
    networks:
      - app-network

  nginx-exporter:
    image: nginx/nginx-prometheus-exporter
    command: ['-nginx.scrape-uri=http://nginx-server:80/stub_status']
    ports:
      - "9113:9113"
    networks:
      - app-network
```

모든 서비스는 `app-network`라는 외부 Docker 네트워크를 공유하여 서로 통신합니다.

### 3-3. Nginx 설정

```nginx
# HTTP → HTTPS 리다이렉트
server {
    listen 80;
    server_name keep4.life www.keep4.life;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;          # Certbot 인증용
    }

    location / {
        return 301 https://$host$request_uri;
    }
}

# HTTPS
server {
    listen 443 ssl;
    server_name keep4.life www.keep4.life;

    ssl_certificate     /etc/letsencrypt/live/keep4.life/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/keep4.life/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;

    add_header Strict-Transport-Security "max-age=63072000" always;

    # API 요청 → Spring Boot 프록시
    location /api/ {
        proxy_pass http://app:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 10M;
    }

    # 정적 파일
    location / {
        root /usr/share/nginx/static;
        try_files $uri $uri/ /index.html;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

> [스크린샷: 브라우저 주소창 - https://keep4.life SSL 인증서 정보]

### 3-4. CI/CD 파이프라인

GitHub Actions 워크플로우 3개로 분리하여, 변경된 부분만 독립적으로 배포합니다.

| 워크플로우 | 트리거 경로 | 배포 대상 |
|-----------|-----------|----------|
| `spring-deploy.yml` | `spring/**` | Spring Boot 컨테이너 |
| `nginx-deploy.yml` | `nginx/**` | Nginx 컨테이너 |
| `etc-deploy.yml` | `deploy/etc/**` | DB, Certbot, Exporter |

**Spring 배포 흐름:**

```
1. GitHub push (production 브랜치)
   │
2. GitHub Actions
   ├── JDK 17 설정
   ├── ./gradlew :spring:clean :spring:build
   ├── Docker 이미지 빌드 (eclipse-temurin:17-jdk-alpine)
   ├── AWS ECR 로그인 + 이미지 푸시
   ├── .env 파일 생성 (GitHub Secrets → 환경변수)
   ├── 배포 번들 압축 (appspec.yml + scripts + docker-compose + .env)
   ├── S3 업로드 (s3://jeongiju-life/spring/<COMMIT_SHA>.tar.gz)
   └── CodeDeploy 트리거
       │
3. EC2 (CodeDeploy Agent)
   ├── BeforeInstall: 기존 컨테이너 중지 + 디렉토리 정리
   └── ApplicationStart: docker pull + docker compose up -d
```

> [스크린샷: GitHub Actions - 배포 워크플로우 성공 화면]

> [스크린샷: AWS CodeDeploy - 배포 성공 화면]

**Dockerfile:**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY ./spring/build/libs/*SNAPSHOT.jar project.jar
ENTRYPOINT ["java", "-jar", "project.jar"]
```

### 3-5. 모니터링

Prometheus가 15초 간격으로 3개 대상에서 메트릭을 수집합니다.

```yaml
# prometheus.yml
scrape_configs:
  - job_name: "spring-app"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["keep4.life:8081"]

  - job_name: "nginx"
    static_configs:
      - targets: ["keep4.life:9113"]

  - job_name: "postgres"
    static_configs:
      - targets: ["keep4.life:9187"]
```

| 수집 대상 | Exporter | 포트 | 메트릭 |
|----------|----------|------|--------|
| Spring Boot | Micrometer (내장) | 8081 | JVM 메모리, GC, HTTP 요청 수/지연시간 |
| Nginx | nginx-prometheus-exporter | 9113 | 연결 수, 요청 수, 활성 연결 |
| PostgreSQL | postgres-exporter | 9187 | 쿼리 수, 연결 수, 테이블 크기 |

> [스크린샷: Grafana 대시보드 - JVM, HTTP, DB 모니터링 화면]

> [스크린샷: Prometheus Targets 페이지 - 3개 대상 연결 상태]

### 3-6. 환경 분리

| 프로파일 | DB | 저장소 | 비밀번호 | 로그 |
|----------|-----|--------|---------|------|
| dev | localhost:5432/app | 로컬 파일 | 평문 | debug |
| local | localhost:5432/appdb | AWS S3 | 평문 | info |
| prod | 환경변수 (RDS) | AWS S3 | BCrypt | 기본 |

모든 민감 정보는 GitHub Secrets → `.env` 파일 → Docker 환경변수로 주입됩니다.

> [스크린샷: GitHub Settings - Secrets and Variables 목록 (값은 마스킹)]

---

## 4. API 명세서

> `{type}`: `check`, `text`, `time`, `number`, `check-list`, `expense`

| 분류 | Method | Path | 설명 | 인증 |
|------|--------|------|------|------|
| 인증 | GET | `/api/auth/csrf-token` | CSRF 토큰 발급 | X |
| 인증 | POST | `/api/auth/sign-in` | 로그인 (form: username, password) | X |
| 인증 | POST | `/api/auth/sign-out` | 로그아웃 | O |
| 인증 | POST | `/api/auth/refresh` | 토큰 갱신 (Cookie: REFRESH_TOKEN) | X |
| 인증 | POST | `/api/auth/reset-password` | 비밀번호 재설정 | X |
| 사용자 | POST | `/api/user` | 회원가입 | X |
| 사용자 | GET | `/api/user` | 내 정보 조회 | O |
| 사용자 | PUT | `/api/user` | 프로필 수정 (Multipart: image + JSON) | O |
| 사용자 | DELETE | `/api/user` | 회원 탈퇴 | O |
| 카테고리 | GET | `/api/category/all` | 내 카테고리 전체 조회 | O |
| 카테고리 | GET | `/api/category?id=` | 카테고리 단건 조회 | O |
| 카테고리 | POST | `/api/category` | 카테고리 생성 | O |
| 카테고리 | PUT | `/api/category` | 카테고리 수정 | O |
| 카테고리 | DELETE | `/api/category?id=` | 카테고리 삭제 (기록 포함) | O |
| 카테고리 | GET | `/api/category/public/summary` | 공개 카테고리 목록 (커서 페이지네이션) | O |
| 카테고리 | GET | `/api/category/public/summary/no-token` | 공개 카테고리 목록 (비로그인) | X |
| 카테고리 | GET | `/api/category/{categoryId}/email` | 좋아요 누른 사용자 목록 | O |
| 기록 | GET | `/api/{type}/all?categoryId=` | 카테고리의 전체 기록 | O |
| 기록 | GET | `/api/{type}/date?categoryId=&date=` | 특정 날짜 기록 | O |
| 기록 | GET | `/api/{type}?{type}Id=` | 단건 조회 | O |
| 기록 | POST | `/api/{type}` | 기록 추가 | O |
| 기록 | PUT | `/api/{type}` | 기록 수정 | O |
| 기록 | DELETE | `/api/{type}?id=` | 기록 삭제 | O |
| 기록 | DELETE | `/api/text/image?imageUrl=` | 텍스트 기록 개별 이미지 삭제 | O |
| 좋아요 | POST | `/api/categories/likes` | 좋아요 추가 | O |
| 좋아요 | DELETE | `/api/categories/likes` | 좋아요 취소 | O |
| 좋아요 | GET | `/api/categories/likes/{categoryId}` | 좋아요 사용자 목록 | O |
| 좋아요 | GET | `/api/categories/likes/count/{categoryId}` | 좋아요 수 | O |
| 좋아요 | GET | `/api/categories/likes/checks/{categoryId}` | 내가 좋아요 눌렀는지 확인 | O |
| 댓글 | GET | `/api/category/comment/{categoryId}` | 댓글 목록 (로그인) | O |
| 댓글 | GET | `/api/category/comment/no-token/{categoryId}` | 댓글 목록 (비로그인) | X |
| 댓글 | POST | `/api/category/comment` | 댓글 작성 | O |
| 댓글 | POST | `/api/category/comment/replies` | 답글 작성 | O |
| 댓글 | PATCH | `/api/category/comment` | 댓글 수정 | O |
| 댓글 | DELETE | `/api/category/comment/{commentId}` | 댓글 삭제 (답글 cascade) | O |
| 태그 | GET | `/api/expense/tag/all` | 내 태그 전체 조회 | O |
| 태그 | GET | `/api/expense/tag?tagId=` | 태그 단건 조회 | O |
| 태그 | POST | `/api/expense/tag` | 태그 생성 | O |
| 태그 | PUT | `/api/expense/tag` | 태그 수정 | O |
| 태그 | DELETE | `/api/expense/tag?id=` | 태그 삭제 | O |
| 친구 | GET | `/api/friends/search?q=` | 사용자 검색 (닉네임/이메일) | O |
| 친구 | POST | `/api/friends/request` | 친구 요청 보내기 | O |
| 친구 | POST | `/api/friends/accept/{friendshipId}` | 친구 요청 수락 | O |
| 친구 | POST | `/api/friends/reject/{friendshipId}` | 친구 요청 거절 | O |
| 친구 | GET | `/api/friends` | 친구 목록 | O |
| 친구 | GET | `/api/friends/pending` | 받은 친구 요청 목록 | O |
| 친구 | DELETE | `/api/friends/{friendshipId}` | 친구 삭제 | O |
| 채팅 | POST | `/api/chat/rooms` | 채팅방 생성/조회 | O |
| 채팅 | GET | `/api/chat/rooms` | 채팅방 목록 | O |
| 채팅 | GET | `/api/chat/rooms/{chatRoomId}/messages?page=&size=` | 메시지 조회 (페이지네이션) | O |
| 채팅 | POST | `/api/chat/rooms/{chatRoomId}/messages` | 메시지 전송 | O |
| 채팅 | POST | `/api/chat/rooms/{chatRoomId}/read` | 읽음 처리 | O |
| 채팅 | GET | `/api/chat/unread` | 전체 안 읽은 메시지 수 | O |
| 알림 | GET | `/api/notification/unread` | 읽지 않은 알림 목록 | O |
| 알림 | GET | `/api/notification/unread/count` | 읽지 않은 알림 수 | O |
| 알림 | POST | `/api/notification/unread` | 알림 읽음 처리 (다건) | O |
| SSE | GET | `/api/sse/subscribe` | SSE 스트림 연결 (text/event-stream) | O |

> [스크린샷: Swagger UI - 전체 API 엔드포인트 목록]

---

## 5. 핵심 기술 상세

### 5-1. JWT 인증

```java
// JwtTokenProvider.java - 토큰 생성
JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
    .subject(user.getEmail())
    .jwtID(UUID.randomUUID().toString())
    .claim("userId", user.getUserId().toString())
    .claim("type", tokenType.getValue())      // "access" 또는 "refresh"
    .claim("authority", "ROLE_USER")
    .issueTime(now)
    .expirationTime(new Date(now.getTime() + expirationMs))
    .build();

SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
signedJWT.sign(signer);  // Access/Refresh 별도 비밀 키
```

- Access Token: 짧은 만료, Authorization 헤더로 전송
- Refresh Token: 긴 만료, HttpOnly 쿠키로 전송
- 사용자당 동시 세션 1개 제한 (InMemoryJwtRegistry, ConcurrentHashMap)
- 5분마다 만료 토큰 정리 스케줄러

### 5-2. SSE 실시간 알림

```java
// SseService.java
ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters;
// 타임아웃: 1시간, 사용자당 최대 3연결, 이벤트 캐시 5분/100건
```

- WebSocket 대신 SSE 선택 (단방향 알림에 적합, HTTP 기반, 자동 재연결)
- 15초 하트비트로 연결 유지
- Last-Event-ID로 놓친 이벤트 복구

### 5-3. 파일 저장소 Strategy 패턴

```java
public interface BinaryStorage {
    void put(UUID userId, byte[] data, String contentType);
    InputStream get(UUID userId);
    String getUrl(UUID userId);
}

@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3BinaryStorage implements BinaryStorage { ... }

@ConditionalOnProperty(name = "storage.type", havingValue = "file")
public class FileBinaryStorage implements BinaryStorage { ... }
```

### 5-4. QueryDSL 커서 페이지네이션

offset 대신 커서 기반 페이지네이션으로 데이터 추가/삭제 시에도 일관된 결과를 보장합니다.

### 5-5. 프론트엔드 authFetch

```javascript
async function authFetch(url, options = {}) {
    headers['Authorization'] = `Bearer ${accessToken}`;
    if (['POST','PUT','DELETE'].includes(method)) {
        headers['X-XSRF-TOKEN'] = getCsrfToken();
    }
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401) {
        await refreshAccessToken();  // Promise 락으로 동시 호출 방지
        return fetch(url, { ...options, headers });  // 재시도
    }
    return response;
}
```

> [스크린샷: 브라우저 개발자 도구 - Network 탭에서 API 요청/응답 흐름]
