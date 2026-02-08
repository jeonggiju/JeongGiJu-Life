# Keep4Life

일상의 다양한 기록을 카테고리별로 관리하고, 다른 사용자와 공유할 수 있는 라이프 트래킹 웹 애플리케이션입니다.

## 주요 기능

- **다양한 기록 타입** : 텍스트, 체크, 시간, 체크리스트, 숫자, 가계부(수입/지출) 총 6가지 타입 지원
- **카테고리 관리** : 기록을 카테고리별로 분류하며, 공개/비공개 설정 가능
- **소셜 기능** : 공개 카테고리에 좋아요, 댓글(대댓글 포함) 기능
- **실시간 알림** : SSE(Server-Sent Events) 기반 실시간 알림
- **이미지 업로드** : AWS S3를 활용한 텍스트 기록 이미지 첨부
- **다국어 지원** : 한국어 / 영어 전환
- **JWT 인증** : Access/Refresh Token 기반 인증, 세션 수 제한

## 기술 스택

### Backend
| 구분 | 기술 |
|------|------|
| Framework | Spring Boot 3.5.9 |
| Language | Java 17 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA, QueryDSL 5.1 |
| Security | Spring Security, JWT (nimbus-jose-jwt) |
| Storage | AWS S3 |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Monitoring | Spring Actuator, Micrometer Prometheus |

### Frontend
| 구분 | 기술 |
|------|------|
| 렌더링 | Vanilla JavaScript SPA |
| 실시간 통신 | Server-Sent Events (SSE) |
| 정적 파일 서빙 | Nginx |

### Infra / DevOps
| 구분 | 기술 |
|------|------|
| Container | Docker |
| CI/CD | GitHub Actions + AWS CodeDeploy |
| Registry | AWS ECR |
| Server | AWS EC2 |
| Reverse Proxy | Nginx (SSL/TLS, Let's Encrypt) |
| Monitoring | Prometheus + Grafana |
| DB Exporter | postgres-exporter, nginx-exporter |

## 프로젝트 구조

```
Keep4Life/
├── spring/                          # Spring Boot 백엔드
│   └── src/main/java/com/life/jeonggiju/
│       ├── config/                  # 설정 (Security, JPA, S3, Swagger, QueryDSL 등)
│       ├── domain/
│       │   ├── user/                # 사용자 (회원가입, 로그인, 프로필)
│       │   ├── category/            # 카테고리, 좋아요, 댓글
│       │   ├── textRecord/          # 텍스트 기록 (이미지 첨부)
│       │   ├── checkRecord/         # 체크 기록 (일별 성공/실패)
│       │   ├── timeRecord/          # 시간 기록
│       │   ├── numberRecord/        # 숫자 기록
│       │   ├── checkListRecord/     # 체크리스트 기록
│       │   ├── expenseRecord/       # 가계부 (수입/지출, 태그)
│       │   └── notification/        # 실시간 알림 (SSE)
│       └── global/                  # 전역 예외 처리, 공통 모듈
├── nginx/                           # Nginx 리버스 프록시
│   ├── conf/default.conf            # Nginx 설정 (SSL, 프록시)
│   └── static/                      # 프론트엔드 정적 파일 (HTML, CSS, JS)
├── monitoring/                      # 모니터링 스택
│   ├── prometheus/                  # Prometheus 설정
│   └── grafana/                     # Grafana 대시보드 및 알림 설정
├── deploy/                          # 배포용 docker-compose 파일
│   ├── spring/                      # 백엔드 컨테이너
│   ├── nginx/                       # Nginx 컨테이너
│   └── etc/                         # DB, Certbot, Exporter 컨테이너
├── postgres/                        # PostgreSQL 초기화 스크립트
├── formatter/                       # Naver IntelliJ 코드 포맷터
└── .github/workflows/               # CI/CD 파이프라인
```

## 도메인 모델

```
User ──< Category ──< TextRecord
                  ──< CheckRecord
                  ──< TimeRecord
                  ──< NumberRecord
                  ──< CheckListRecord
                  ──< ExpenseRecord ──< ExpenseRecordTag >── ExpenseTag
                  ──< Comment (self-referencing: 대댓글)
                  ──< CategoryLike >── User
```

## API 엔드포인트

Swagger UI를 통해 전체 API 문서를 확인할 수 있습니다: `https://keep4.life/swagger-ui.html`

| 도메인 | 주요 엔드포인트 |
|--------|----------------|
| 인증 | `POST /api/auth/sign-up`, `POST /api/auth/sign-in`, `POST /api/auth/sign-out` |
| 사용자 | `GET /api/user`, `PATCH /api/user` |
| 카테고리 | `GET/POST/PATCH/DELETE /api/category` |
| 텍스트 기록 | `GET/POST/PATCH/DELETE /api/text` |
| 체크 기록 | `GET/POST/PATCH/DELETE /api/check` |
| 시간 기록 | `GET/POST/PATCH/DELETE /api/time` |
| 숫자 기록 | `GET/POST/PATCH/DELETE /api/number` |
| 체크리스트 | `GET/POST/PATCH/DELETE /api/checklist` |
| 가계부 | `GET/POST/PATCH/DELETE /api/expense` |
| 가계부 태그 | `GET/POST/PATCH/DELETE /api/expense-tag` |
| 좋아요 | `POST/DELETE /api/like` |
| 댓글 | `POST/PATCH/DELETE /api/comment` |
| 알림 | `GET /api/notification`, SSE 구독 |

## 로컬 개발 환경 설정

### 사전 요구사항

- Java 17+
- PostgreSQL 16
- Docker & Docker Compose (선택)

### 실행 방법

1. PostgreSQL 데이터베이스 생성
```bash
createdb -U postgres app
```

2. 프로젝트 빌드 및 실행
```bash
cd spring
./gradlew bootRun --args='--spring.profiles.active=dev'
```

3. 접속
    - API: `http://localhost:8080`
    - Swagger UI: `http://localhost:8080/swagger-ui.html`

### 환경 변수 (Production)

| 변수명 | 설명 |
|--------|------|
| `SPRING_PROFILES_ACTIVE` | 프로파일 (`dev`, `local`, `prod`) |
| `SPRING_DATASOURCE_URL` | DB 접속 URL |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자명 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |
| `JWT_ACCESS_KEY` | JWT Access Token 서명 키 |
| `JWT_REFRESH_KEY` | JWT Refresh Token 서명 키 |
| `JWT_ACCESS_TOKEN_EXPIRCATION_MS` | Access Token 만료 시간 (ms) |
| `JWT_REFRESH_TOKEN_EXPIRCATION_MS` | Refresh Token 만료 시간 (ms) |
| `AWS_S3_ACCESS_KEY` | AWS S3 Access Key |
| `AWS_S3_SECRET_KEY` | AWS S3 Secret Key |
| `AWS_S3_REGION` | AWS 리전 |
| `AWS_S3_BUCKET` | S3 버킷명 |
| `INIT_ADMIN_EMAIL` | 초기 관리자 이메일 |
| `INIT_ADMIN_PASSWORD` | 초기 관리자 비밀번호 |

## 배포

GitHub Actions를 통해 `production` 브랜치에 push 시 자동 배포됩니다.

1. Gradle 빌드 및 Docker 이미지 생성
2. AWS ECR에 이미지 Push
3. AWS CodeDeploy를 통해 EC2에 배포

```
production branch push
  → GitHub Actions
    → Docker Build → ECR Push
    → S3 Artifact Upload → CodeDeploy → EC2
```

## 모니터링

Prometheus + Grafana 기반으로 다음 메트릭을 수집합니다:

- **Spring Boot**: Actuator + Micrometer (포트 8081)
- **Nginx**: nginx-prometheus-exporter (포트 9113)
- **PostgreSQL**: postgres-exporter (포트 9187)

```bash
cd monitoring
docker-compose up -d
```

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
