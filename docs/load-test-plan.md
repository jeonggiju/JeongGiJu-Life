# Keep4Life JMeter 부하 테스트 계획서

## 1. 테스트 개요

### 1-1. 목적

| 항목 | 내용 |
|------|------|
| **Saturation Point** | t3.small(2 vCPU, 2GB RAM)에서 서버가 정상 응답 불가 직전의 최대 동시 접속자 수 파악 |
| **Latency Analysis** | 부하 증가에 따른 주요 API 응답 시간(p50, p95, p99) 변화 측정 |
| **Resource Monitoring** | CPU Credit 고갈 시점, 메모리 스왑 발생 지점, DB 커넥션 포화 시점 확인 |

### 1-2. 대상 환경

```
[JMeter GUI (로컬)]                    [Prometheus + Grafana (로컬)]
        │                                       │
        ▼ HTTPS                                  ▼ :8081 (Actuator)
[EC2 t3.small - ap-northeast-2]                 ▼ :9113 (nginx-exporter)
  ┌─────────────────────────────────────┐        ▼ :9104 (mysql-exporter)
  │  Docker Compose (app-network)       │
  │                                     │
  │  Nginx (:80/:443)                   │
  │    → Spring Boot (:8080)            │
  │         → MySQL (:3306)             │
  │                                     │
  │  nginx-exporter (:9113)             │
  │  mysql-exporter (:9104)             │
  └─────────────────────────────────────┘
```

| 항목 | 스펙 |
|------|------|
| EC2 | t3.small (2 vCPU, 2GB RAM, Burstable) |
| CPU Credit | 기본 12크레딧, 시간당 12크레딧 적립 |
| EC2 컨테이너 | 5개 (Spring Boot, MySQL, Nginx, nginx-exporter, mysql-exporter) |
| 로컬 | Prometheus + Grafana (EC2 부하 제거 목적) |
| JMeter | GUI 모드 (로컬 실행) |
| JVM | eclipse-temurin:17-jdk-alpine (별도 힙 설정 없음 → 기본 ~512MB) |
| DB | MySQL 8.0, HikariCP 기본 풀 (max 10) |

### 1-3. 성능 기준 (Pass/Fail Criteria)

| 지표 | 정상 | 경고 | 실패 |
|------|------|------|------|
| 평균 응답 시간 | < 500ms | 500ms ~ 2s | > 2s |
| p95 응답 시간 | < 1s | 1s ~ 3s | > 3s |
| 에러율 | < 1% | 1% ~ 5% | > 5% |
| CPU 사용률 | < 70% | 70% ~ 90% | > 90% |
| 메모리 사용률 | < 80% | 80% ~ 95% | > 95% |

---

## 2. 사전 준비

### 2-1. JMeter 설치

```bash
# macOS
brew install jmeter

# Linux
wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz
export PATH=$PATH:$(pwd)/apache-jmeter-5.6.3/bin
```

### 2-2. 테스트 데이터 규모

빈 테이블에서의 부하 테스트는 실제 성능과 완전히 다른 결과를 냅니다. 인덱스 효과와 JOIN 성능이 드러나려면 테이블당 최소 수만 건 이상이 필요합니다.

```
데이터 0건        → 쿼리 0ms    (의미 없음)
데이터 100건      → 풀스캔도 0ms (인덱스 효과 확인 불가)
데이터 10,000건~  → 인덱스 유무에 따라 성능 차이 발생
데이터 100,000건~ → JOIN, 페이지네이션, 정렬 성능이 드러남 ← 이 구간
```

| 테이블 | 데이터 수 | 산출 근거 |
|--------|----------|----------|
| **users** | **500명** | 테스트 유저 200 + 기존 유저 300 |
| **category** | **2,500개** | 유저당 5개 (공개 3 + 비공개 2) |
| **check_record** | **150,000건** | 카테고리당 180일치 (6개월) |
| **text_record** | **50,000건** | 카테고리당 ~100건 |
| **text_record_image** | **100,000건** | 텍스트 기록당 이미지 2장 |
| **time_record** | **100,000건** | check_record와 유사 패턴 |
| **number_record** | **100,000건** | check_record와 유사 패턴 |
| **check_list_record** | **75,000건** | 하루 3개 항목 * 180일 |
| **expense_record** | **75,000건** | 하루 3건 * 180일 |
| **expense_tag** | **2,500개** | 유저당 5개 |
| **expense_record_tag** | **100,000건** | 지출 기록당 태그 1~2개 |
| **category_like** | **10,000건** | 공개 카테고리당 좋아요 ~10개 |
| **comment** | **15,000건** | 공개 카테고리당 댓글 ~6개 (대댓글 포함) |
| **friendship** | **5,000건** | 유저당 친구 ~10명 |
| **chat_room** | **2,500개** | 친구 쌍당 1개 |
| **chat_message** | **125,000건** | 채팅방당 메시지 ~50건 |
| **notification** | **50,000건** | 유저당 ~100건 |
| **총합** | **약 86만건** | t3.small MySQL에서 충분히 감당 가능 |

특히 성능에 영향을 주는 핵심 쿼리:
- `category/public/summary` → category + like + comment **JOIN + 커서 페이지네이션**
- `comment/{categoryId}` → **self-referencing 대댓글 트리** 조회
- `expense/all` → expense_record + expense_tag **M:N JOIN**

### 2-3. 테스트 데이터 생성 SQL

#### 유저 + 카테고리

```sql
-- 1. 유저 500명
-- MySQL에서는 프로시저를 사용하거나 반복 INSERT 필요
DELIMITER //
CREATE PROCEDURE generate_test_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 500 DO
        INSERT INTO users (id, email, username, nickname, password, authority)
        VALUES (
            UUID(),
            CONCAT('loadtest', i, '@test.com'),
            CONCAT('loaduser', i),
            CONCAT('tester', i),
            'test1234',
            'ROLE_USER'
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL generate_test_users();
DROP PROCEDURE generate_test_users;

-- 2. 카테고리 (유저당 5개, 타입 분배, 공개/비공개 혼합)
DELIMITER //
CREATE PROCEDURE generate_test_categories()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE user_id_var CHAR(36);
    DECLARE n INT;
    DECLARE cur CURSOR FOR SELECT id FROM users WHERE email LIKE 'loadtest%';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO user_id_var;
        IF done THEN LEAVE read_loop; END IF;
        SET n = 1;
        WHILE n <= 5 DO
            INSERT INTO category (id, user_id, title, description, record_type, visibility, created_at)
            VALUES (
                UUID(),
                user_id_var,
                CONCAT('카테고리 ', n),
                '부하 테스트용',
                ELT(n, 'CHECK', 'TEXT', 'TIME', 'NUMBER', 'EXPENSE'),
                IF(n <= 3, 'PUBLIC', 'PRIVATE'),
                DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
            );
            SET n = n + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_test_categories();
DROP PROCEDURE generate_test_categories;
```

#### 기록 데이터 (6종)

```sql
-- 3. check_record (CHECK 카테고리당 180일치)
DELIMITER //
CREATE PROCEDURE generate_check_records()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE cat_id CHAR(36);
    DECLARE d INT;
    DECLARE cur CURSOR FOR
        SELECT c.id FROM category c
        JOIN users u ON c.user_id = u.id
        WHERE c.record_type = 'CHECK' AND u.email LIKE 'loadtest%';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO cat_id;
        IF done THEN LEAVE read_loop; END IF;
        SET d = 0;
        WHILE d < 180 DO
            INSERT INTO check_record (id, category_id, success, date)
            VALUES (UUID(), cat_id, RAND() > 0.3, DATE_SUB(CURDATE(), INTERVAL d DAY));
            SET d = d + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_check_records();
DROP PROCEDURE generate_check_records;

-- 4. text_record (TEXT 카테고리당 100건)
DELIMITER //
CREATE PROCEDURE generate_text_records()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE cat_id CHAR(36);
    DECLARE n INT;
    DECLARE cur CURSOR FOR
        SELECT c.id FROM category c
        JOIN users u ON c.user_id = u.id
        WHERE c.record_type = 'TEXT' AND u.email LIKE 'loadtest%';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO cat_id;
        IF done THEN LEAVE read_loop; END IF;
        SET n = 1;
        WHILE n <= 100 DO
            INSERT INTO text_record (id, category_id, title, text, date)
            VALUES (
                UUID(),
                cat_id,
                CONCAT('텍스트 기록 ', n),
                CONCAT('부하 테스트 텍스트 내용 ', n),
                DATE_SUB(CURDATE(), INTERVAL FLOOR(n * 1.8) DAY)
            );
            SET n = n + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_text_records();
DROP PROCEDURE generate_text_records;

-- time_record, number_record, check_list_record, expense_record도
-- 위와 동일한 패턴으로 생성 (record_type 조건만 변경)
```

#### 소셜 데이터

```sql
-- 5. category_like (공개 카테고리당 랜덤 좋아요 ~10개)
DELIMITER //
CREATE PROCEDURE generate_category_likes()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE cat_id CHAR(36);
    DECLARE user_id_var CHAR(36);
    DECLARE i INT;
    DECLARE cur CURSOR FOR SELECT id FROM category WHERE visibility = 'PUBLIC';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO cat_id;
        IF done THEN LEAVE read_loop; END IF;
        SET i = 0;
        WHILE i < 10 DO
            SELECT id INTO user_id_var FROM users WHERE email LIKE 'loadtest%' ORDER BY RAND() LIMIT 1;
            INSERT IGNORE INTO category_like (id, user_id, category_id, created_at)
            VALUES (UUID(), user_id_var, cat_id, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY));
            SET i = i + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_category_likes();
DROP PROCEDURE generate_category_likes;

-- 6. comment (공개 카테고리당 댓글 ~5개)
DELIMITER //
CREATE PROCEDURE generate_comments()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE cat_id CHAR(36);
    DECLARE user_id_var CHAR(36);
    DECLARE n INT;
    DECLARE cur CURSOR FOR SELECT id FROM category WHERE visibility = 'PUBLIC';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO cat_id;
        IF done THEN LEAVE read_loop; END IF;
        SET n = 1;
        WHILE n <= 5 DO
            SELECT id INTO user_id_var FROM users WHERE email LIKE 'loadtest%' ORDER BY RAND() LIMIT 1;
            INSERT INTO comment (id, comment, parent_id, category_id, user_id, created_at)
            VALUES (
                UUID(),
                CONCAT('테스트 댓글 ', n),
                NULL,
                cat_id,
                user_id_var,
                DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 1000) HOUR)
            );
            SET n = n + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_comments();
DROP PROCEDURE generate_comments;

-- 7. friendship (유저당 친구 ~10명)
DELIMITER //
CREATE PROCEDURE generate_friendships()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE user1_id CHAR(36);
    DECLARE user2_id CHAR(36);
    DECLARE i INT;
    DECLARE cur CURSOR FOR SELECT id FROM users WHERE email LIKE 'loadtest%';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO user1_id;
        IF done THEN LEAVE read_loop; END IF;
        SET i = 0;
        WHILE i < 10 DO
            SELECT id INTO user2_id FROM users
            WHERE id != user1_id AND email LIKE 'loadtest%' ORDER BY RAND() LIMIT 1;
            INSERT IGNORE INTO friendship (id, requester_id, receiver_id, status, created_at)
            VALUES (UUID(), user1_id, user2_id, 'ACCEPTED', DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY));
            SET i = i + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_friendships();
DROP PROCEDURE generate_friendships;
```

#### 채팅 + 알림

```sql
-- 8. chat_room (친구 쌍당 채팅방)
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
SELECT
    UUID(),
    LEAST(requester_id, receiver_id),
    GREATEST(requester_id, receiver_id),
    created_at,
    NOW()
FROM friendship
WHERE status = 'ACCEPTED'
LIMIT 2500;

-- 9. chat_message (채팅방당 50건)
DELIMITER //
CREATE PROCEDURE generate_chat_messages()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE room_id, u1, u2 CHAR(36);
    DECLARE room_created DATETIME;
    DECLARE n INT;
    DECLARE cur CURSOR FOR SELECT id, user1_id, user2_id, created_at FROM chat_room;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO room_id, u1, u2, room_created;
        IF done THEN LEAVE read_loop; END IF;
        SET n = 1;
        WHILE n <= 50 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (
                UUID(),
                room_id,
                IF(RAND() > 0.5, u1, u2),
                CONCAT('테스트 메시지 ', n),
                RAND() > 0.3,
                DATE_ADD(room_created, INTERVAL n MINUTE)
            );
            SET n = n + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_chat_messages();
DROP PROCEDURE generate_chat_messages;

-- 10. notification (유저당 100건)
DELIMITER //
CREATE PROCEDURE generate_notifications()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE user_id_var, sender_id_var CHAR(36);
    DECLARE n INT;
    DECLARE notification_types VARCHAR(255) DEFAULT 'COMMENT,LIKE,REPLY,FRIEND_REQUEST,FRIEND_ACCEPT';
    DECLARE cur CURSOR FOR SELECT id FROM users WHERE email LIKE 'loadtest%';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO user_id_var;
        IF done THEN LEAVE read_loop; END IF;
        SET n = 1;
        WHILE n <= 100 DO
            SELECT id INTO sender_id_var FROM users
            WHERE id != user_id_var AND email LIKE 'loadtest%' ORDER BY RAND() LIMIT 1;
            INSERT INTO notification (id, receiver_id, sender_id, type, data, `read`, created_at)
            VALUES (
                UUID(),
                user_id_var,
                sender_id_var,
                ELT(FLOOR(RAND() * 5) + 1, 'COMMENT', 'LIKE', 'REPLY', 'FRIEND_REQUEST', 'FRIEND_ACCEPT'),
                '{"categoryTitle":"테스트"}',
                RAND() > 0.5,
                DATE_SUB(NOW(), INTERVAL n MINUTE)
            );
            SET n = n + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL generate_notifications();
DROP PROCEDURE generate_notifications;
```

#### 데이터 생성 후 필수 작업

```sql
-- 통계 갱신 (쿼리 옵티마이저가 올바른 실행 계획을 세우도록)
ANALYZE TABLE users, category, check_record, text_record, time_record,
    number_record, check_list_record, expense_record, expense_tag, expense_record_tag,
    category_like, comment, friendship, chat_room, chat_message, notification;
```

### 2-4. CSV 데이터 파일

JMeter에서 사용할 CSV 파일을 준비합니다.

**users.csv:**
```csv
email,password
loadtest1@test.com,test1234
loadtest2@test.com,test1234
loadtest3@test.com,test1234
...
```

생성 스크립트:

```bash
echo "email,password" > users.csv
for i in $(seq 1 200); do
    echo "loadtest${i}@test.com,test1234" >> users.csv
done
```

---

## 3. 인증 플로우 (JMeter 선행 설정)

Keep4Life는 **CSRF + Form Login + JWT** 방식입니다. 모든 인증 필요 API 호출 전에 아래 플로우를 반드시 거쳐야 합니다.

```
Step 1: GET  /api/auth/csrf-token     → Response Header에서 X-CSRF-TOKEN 추출
Step 2: POST /api/auth/sign-in        → Form Data (username, password) + X-XSRF-TOKEN 헤더
                                       → Response Body에서 accessToken 추출
                                       → Response Cookie에서 REFRESH_TOKEN 추출
Step 3: 이후 모든 요청에 Authorization: Bearer {accessToken} 헤더 포함
        POST/PUT/DELETE 요청에 X-XSRF-TOKEN 헤더 포함
```

### JMeter 설정 단계

#### Step 1 - CSRF 토큰 획득

| 항목 | 설정값 |
|------|--------|
| Sampler | HTTP Request |
| Method | GET |
| Path | /api/auth/csrf-token |
| **Post-Processor** | Regular Expression Extractor |
| Apply to | Response Headers |
| Reference Name | `csrfToken` |
| Regular Expression | `X-CSRF-TOKEN: (.+?)$` |

추가로 Response Cookie에서 `XSRF-TOKEN`도 자동 추출되도록 **HTTP Cookie Manager**를 Thread Group에 추가합니다.

#### Step 2 - 로그인

| 항목 | 설정값 |
|------|--------|
| Sampler | HTTP Request |
| Method | POST |
| Path | /api/auth/sign-in |
| Content-Type | application/x-www-form-urlencoded |
| Body Data | `username=${email}&password=${password}` |
| Header | `X-XSRF-TOKEN: ${csrfToken}` |
| **Post-Processor** | JSON Extractor |
| JSON Path | `$.accessToken` |
| Variable Name | `accessToken` |

#### Step 3 - 인증 헤더 공통 설정

**HTTP Header Manager** (Thread Group 레벨):

| Header Name | Value |
|-------------|-------|
| Authorization | `Bearer ${accessToken}` |
| X-XSRF-TOKEN | `${csrfToken}` |

---

## 4. 테스트 시나리오 설계

### 4-1. 시나리오 구성 비율

실제 사용 패턴을 반영하여 읽기 80%, 쓰기 20%로 배분합니다.

| 시나리오 | 비율 | 설명 | 인증 |
|----------|------|------|------|
| S1. 공개 피드 조회 | 25% | 비로그인 사용자의 카테고리 목록 탐색 | X |
| S2. 내 카테고리 + 기록 조회 | 25% | 로그인 후 카테고리 목록 → 기록 조회 | O |
| S3. 기록 생성 (Check) | 10% | 체크 기록 추가 | O |
| S4. 좋아요 토글 | 10% | 좋아요 추가/취소 반복 | O |
| S5. 댓글 작성 + 조회 | 10% | 댓글 작성 후 목록 조회 | O |
| S6. SSE 연결 유지 | 15% | SSE 스트림 구독 후 연결 유지 | O |
| S7. 채팅 메시지 전송 | 5% | 채팅방 메시지 전송 + 조회 | O |

### 4-2. 시나리오별 상세 요청 흐름

**S1. 공개 피드 조회 (비인증)**

```
1. GET /api/category/public/summary/no-token?size=20
2. Think Time: 2~5초 (Random Timer)
3. GET /api/category/public/summary/no-token?size=20&cursor={nextCursor}
4. GET /api/category/comment/no-token/{categoryId}
```

**S2. 내 카테고리 + 기록 조회 (인증)**

```
1. [인증 플로우 - Step 1~2]
2. GET /api/category/all
3. Think Time: 1~3초
4. GET /api/check/all?categoryId={categoryId}
5. GET /api/check/date?categoryId={categoryId}&date=2026-02-08
```

**S3. 기록 생성 (인증)**

```
1. [인증 플로우 - Step 1~2]
2. POST /api/check
   Body: {"categoryId":"${categoryId}","success":true,"date":"2026-02-08"}
3. GET /api/check/all?categoryId={categoryId}   ← 생성 후 조회 검증
```

**S4. 좋아요 토글 (인증)**

```
1. [인증 플로우 - Step 1~2]
2. GET /api/category/public/summary?size=10
3. POST /api/categories/likes  Body: {"categoryId":"${categoryId}"}
4. GET /api/categories/likes/count/{categoryId}
5. DELETE /api/categories/likes  Body: {"categoryId":"${categoryId}"}
```

**S5. 댓글 작성 + 조회 (인증)**

```
1. [인증 플로우 - Step 1~2]
2. POST /api/category/comment  Body: {"categoryId":"${categoryId}","comment":"부하테스트 댓글"}
3. GET /api/category/comment/{categoryId}
```

**S6. SSE 연결 유지 (인증)**

```
1. [인증 플로우 - Step 1~2]
2. GET /api/sse/subscribe
   - Response Timeout: 300000ms (5분)
   - 연결을 유지하면서 다른 Thread Group의 알림 이벤트 수신
```

> SSE는 장시간 연결을 유지하므로 **별도의 Thread Group**으로 분리합니다.

**S7. 채팅 메시지 전송 (인증)**

```
1. [인증 플로우 - Step 1~2]
2. POST /api/chat/rooms  Body: {"friendId":"${friendId}"}
3. POST /api/chat/rooms/${chatRoomId}/messages  Body: {"content":"부하테스트 메시지"}
4. GET /api/chat/rooms/${chatRoomId}/messages?page=0&size=20
```

---

## 5. JMeter Thread Group 설정

### 5-1. 단계별 부하 증가 전략 (Stepping Strategy)

한 번에 몰아넣지 않고 **5단계**로 점진적으로 부하를 올립니다. 각 단계에서 안정화된 후 다음 단계로 진행합니다.

| 단계 | 동시 사용자 | Ramp-up | 유지 시간 | 목적 |
|------|------------|---------|----------|------|
| **Step 1** | 5명 | 5초 | 3분 | Baseline (정상 응답 확인) |
| **Step 2** | 15명 | 15초 | 5분 | 경량 부하 (Warm-up) |
| **Step 3** | 30명 | 30초 | 5분 | 중간 부하 (일반 사용 패턴) |
| **Step 4** | 50명 | 60초 | 5분 | 고부하 (피크 트래픽 시뮬레이션) |
| **Step 5** | 80~100명 | 120초 | 5분 | 극한 부하 (Saturation Point 탐색) |

> **t3.small (2GB RAM) 기준:** available 메모리가 약 878MB밖에 안 되므로 t3.medium 대비 부하 단계를 절반 수준으로 낮췄습니다.

### 5-2. Thread Group 구조

```
Test Plan
├── CSV Data Set Config (users.csv)
├── HTTP Cookie Manager
├── HTTP Header Manager (공통 헤더)
│
├── [Thread Group 1] 메인 시나리오 (S1~S5, S7)
│   ├── Threads: ${USERS}       ← 단계별 변경 (5 → 15 → 30 → 50 → 100)
│   ├── Ramp-up: ${RAMPUP}     ← 단계별 변경
│   ├── Loop Count: -1 (Infinite)
│   ├── Duration: ${DURATION}   ← 단계별 변경 (180~300초)
│   │
│   ├── [Once Only Controller] - 인증 플로우
│   │   ├── GET  /api/auth/csrf-token  + RegEx Extractor
│   │   └── POST /api/auth/sign-in     + JSON Extractor
│   │
│   ├── [Throughput Controller 25%] S1. 공개 피드 조회
│   ├── [Throughput Controller 25%] S2. 카테고리+기록 조회
│   ├── [Throughput Controller 10%] S3. 기록 생성
│   ├── [Throughput Controller 10%] S4. 좋아요 토글
│   ├── [Throughput Controller 10%] S5. 댓글 작성
│   ├── [Throughput Controller  5%] S7. 채팅 메시지
│   │
│   └── [Gaussian Random Timer] 1000~3000ms (Think Time)
│
├── [Thread Group 2] SSE 연결 유지 (S6)
│   ├── Threads: ${USERS} * 0.15  ← 전체의 15%
│   ├── Ramp-up: 30초
│   ├── Loop Count: 1
│   │
│   ├── [Once Only Controller] - 인증 플로우
│   └── GET /api/sse/subscribe (Response Timeout: 300000ms)
│
└── Listeners
    ├── Summary Report
    ├── Aggregate Report
    ├── Response Times Over Time
    ├── Active Threads Over Time
    └── Transactions per Second
```

### 5-3. JMeter GUI 모드 설정 방법

GUI 모드에서 단계별로 Thread Group 설정을 직접 변경하면서 실행합니다.

**Thread Group 설정 위치:** Thread Group 우클릭 → Properties

| 단계 | Number of Threads | Ramp-Up Period | Duration (Scheduler) |
|------|-------------------|----------------|---------------------|
| Step 1 | 5 | 5 | 180 |
| Step 2 | 15 | 15 | 300 |
| Step 3 | 30 | 30 | 300 |
| Step 4 | 50 | 60 | 300 |
| Step 5 | 100 | 120 | 300 |

> Loop Count은 `Infinite`로 체크하고, `Specify Thread lifetime` → Duration에 초 단위로 입력합니다.

**GUI 모드 주의사항:**
- Listener(Summary Report, Aggregate Report 등)가 많으면 JMeter 자체가 메모리를 많이 사용합니다
- **100 Thread 이상**부터는 GUI에서 결과를 실시간으로 보면 JMeter 자체가 느려질 수 있습니다
- 이 경우 Listener를 비활성화하고, 결과를 `.jtl` 파일로 저장한 뒤 나중에 분석합니다
- `.jtl` 저장: Thread Group → Add → Listener → Simple Data Writer → Filename에 경로 지정

---

## 6. 모니터링 체크리스트

### 6-1. 테스트 중 실시간 모니터링 명령어

SSH로 EC2에 접속한 상태에서 아래 명령어를 실행합니다.

```bash
# 터미널 1: 컨테이너별 CPU/메모리 실시간 모니터링
docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"

# 터미널 2: EC2 호스트 전체 리소스
vmstat 5                     # 5초 간격 CPU/메모리/스왑
iostat -x 5                  # 5초 간격 디스크 I/O

# 터미널 3: Spring Boot 로그 실시간 확인
docker logs -f jeonggiju-life-server --tail 100

# 터미널 4: MySQL 연결 수 확인
docker exec mysql-db mysql -uroot -prootpassword -e "SHOW STATUS LIKE 'Threads_connected';"
```

### 6-2. 중점 모니터링 지표

#### EC2 호스트 (CloudWatch + CLI)

| 지표 | 확인 방법 | 위험 기준 |
|------|----------|----------|
| **CPU Credit Balance** | CloudWatch → EC2 → CPUCreditBalance | < 10 크레딧 |
| **CPU Utilization** | CloudWatch → EC2 → CPUUtilization | > 90% 지속 |
| **Memory 사용률** | `free -m` 또는 CloudWatch Agent | Available < 100MB |
| **Swap 사용** | `swapon -s`, `vmstat` 의 si/so 컬럼 | si/so > 0 이면 스왑 발생 |
| **Network I/O** | CloudWatch → NetworkIn/Out | 급격한 drop = 포화 |

> **t3.small CPU Credit 주의:** Burstable 인스턴스이므로 크레딧이 0이 되면 CPU가 베이스라인(20%)으로 제한됩니다. 부하 테스트 시 CloudWatch에서 `CPUCreditBalance`를 반드시 추적해야 합니다. 크레딧 소진 후에는 Unlimited 모드 활성화 또는 테스트 중단 필요합니다.

#### Spring Boot (Prometheus + Grafana)

| 지표 | PromQL | 의미 |
|------|--------|------|
| HTTP 요청 처리율 | `rate(http_server_requests_seconds_count[1m])` | 초당 요청 수 (RPS) |
| HTTP 응답 시간 p95 | `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[1m]))` | 95% 응답 시간 |
| HTTP 5xx 에러율 | `rate(http_server_requests_seconds_count{status=~"5.."}[1m])` | 서버 에러 발생률 |
| JVM Heap 사용률 | `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}` | > 85% 위험 |
| JVM GC Pause | `rate(jvm_gc_pause_seconds_sum[1m])` | GC에 소비된 시간 |
| HikariCP Active | `hikaricp_connections_active` | 활성 DB 커넥션 (max 10) |
| HikariCP Pending | `hikaricp_connections_pending` | > 0 이면 커넥션 풀 포화 |
| Tomcat Threads Busy | `tomcat_threads_busy_threads` | 활성 처리 스레드 수 |
| Tomcat Threads Max | `tomcat_threads_config_max_threads` | 최대 스레드 (기본 200) |

#### MySQL (mysql-exporter)

| 지표 | 확인 방법 | 위험 기준 |
|------|----------|----------|
| Active Connections | `mysql_global_status_threads_connected` | > max_connections 의 80% |
| Slow Query | `mysql_global_status_slow_queries` | 급격한 증가 |
| Lock Waits | `mysql_global_status_innodb_row_lock_waits` | 급격한 증가 |

#### Nginx (nginx-exporter)

| 지표 | 확인 방법 | 위험 기준 |
|------|----------|----------|
| Active Connections | `nginx_connections_active` | > worker_connections |
| Waiting Connections | `nginx_connections_waiting` | 급격한 감소 (커넥션 포화) |
| Request Rate | `rate(nginx_http_requests_total[1m])` | 처리량 감소 시작 시점 |

### 6-3. Grafana 대시보드 구성

테스트 전에 Grafana에 아래 3개 패널을 구성해두면 실시간으로 병목 지점을 파악할 수 있습니다.

| 패널 | 포함 지표 |
|------|----------|
| **System Overview** | 컨테이너별 CPU%, Memory%, Network I/O |
| **Spring Boot** | RPS, Response Time (p50/p95/p99), Error Rate, Tomcat Threads, HikariCP |
| **Database** | Active Connections, Query Duration, Lock Count |

---

## 7. 테스트 실행 절차

### 7-1. 테스트 전 체크리스트

| # | 항목 | 확인 |
|---|------|------|
| 1 | 테스트 데이터 생성 완료 (2-3절 SQL 실행 + ANALYZE) | [ ] |
| 2 | 테스트 사용자 500명 DB에 존재 확인 | [ ] |
| 3 | users.csv 파일 준비 완료 | [ ] |
| 4 | EC2 CPU Credit Balance 확인 (최소 100 이상) | [ ] |
| 5 | `docker stats` 기본 상태 기록 (baseline) | [ ] |
| 6 | Grafana 대시보드 열어둠 | [ ] |
| 7 | Spring Boot 로그 레벨: WARN 이상으로 변경 (debug 로그 부하 제거) | [ ] |
| 8 | 운영 중인 서비스가 아닌 **테스트 환경**에서 실행 | [ ] |

### 7-2. 단계별 실행 및 판단 기준

```
Step 1 (5명, 3분)
  └─ 통과 조건: 에러율 0%, 평균 응답 < 200ms
     ├─ 통과 → Step 2로 진행
     └─ 실패 → 테스트 중단, 설정 점검

Step 2 (15명, 5분)
  └─ 통과 조건: 에러율 < 1%, 평균 응답 < 500ms
     ├─ 통과 → Step 3로 진행
     └─ 실패 → 이 구간이 한계, 결과 기록

Step 3 (30명, 5분)
  └─ 통과 조건: 에러율 < 1%, p95 < 1s
     ├─ 통과 → Step 4로 진행
     └─ 실패 → 병목 분석 (HikariCP? Tomcat Thread? CPU Credit? OOM?)

Step 4 (50명, 5분)
  └─ 통과 조건: 에러율 < 5%, p95 < 2s
     ├─ 통과 → Step 5로 진행
     └─ 실패 → Saturation Point = ~50명 부근

Step 5 (100명, 5분)
  └─ 이 단계는 **의도적 과부하** 테스트
     └─ 에러율, 응답 시간, CPU Credit 소진 속도, OOM 발생 여부 기록
```

> **각 Step 사이에 3~5분 쿨다운 타임**을 두고, `docker stats`와 CPU Credit을 확인한 후 다음 Step을 진행합니다.

### 7-3. 테스트 실행 (GUI 모드)

1. JMeter GUI에서 `.jmx` 파일을 열고 Thread Group의 Number of Threads / Ramp-Up / Duration을 해당 Step 값으로 설정
2. 상단 초록색 ▶ 버튼 클릭으로 실행
3. Listener(Summary Report, Aggregate Report)에서 실시간으로 결과 확인
4. 상단 ■ 버튼으로 테스트 중단

> **100 Thread 이상 실행 시** Listener가 메모리를 많이 사용하므로, 결과를 `.jtl` 파일로만 저장하고 테스트 완료 후 분석하는 것을 권장합니다.

---

## 8. 결과 분석 방법

### 8-1. JMeter Report에서 확인할 항목

테스트 완료 후 생성되는 HTML Report (`results/stepN-report/index.html`)에서 확인:

| 항목 | 위치 | 판단 기준 |
|------|------|----------|
| **APDEX** | Dashboard > APDEX | > 0.9 양호, < 0.7 불량 |
| **Error %** | Statistics 탭 | 시나리오별 에러율 |
| **Response Time (p95)** | Statistics 탭 | API별 p95 비교 |
| **Throughput** | Statistics 탭 | 초당 처리 요청 수 |
| **Response Times Over Time** | Charts 탭 | 시간에 따른 응답 시간 변화 추이 |
| **TPS Over Time** | Charts 탭 | 처리량이 꺾이는 시점 = Saturation |

### 8-2. Saturation Point 판별법

```
TPS (Transactions Per Second) 그래프에서:

    TPS
     │        ┌──────── ← Saturation Point (TPS 더 이상 안 올라감)
     │       /
     │      /
     │     /
     │    /
     │   /
     │  /
     └──────────────────── 동시 사용자 수
       10   30   50  100  200

TPS가 더 이상 증가하지 않거나 오히려 감소하면서 응답 시간이 급격히 올라가는 지점이
해당 인프라의 Maximum Capacity입니다.
```

### 8-3. 결과 기록 템플릿

각 Step 완료 후 아래 표를 채웁니다.

| 항목 | Step1 (5) | Step2 (15) | Step3 (30) | Step4 (50) | Step5 (100) |
|------|-----------|-----------|-----------|------------|------------|
| 평균 응답 (ms) | | | | | |
| p95 응답 (ms) | | | | | |
| p99 응답 (ms) | | | | | |
| TPS | | | | | |
| 에러율 (%) | | | | | |
| CPU 사용률 (%) | | | | | |
| CPU Credit 잔여 | | | | | |
| Memory 사용률 (%) | | | | | |
| HikariCP Active | | | | | |
| HikariCP Pending | | | | | |
| Tomcat Busy Threads | | | | | |
| Swap 발생 여부 | | | | | |

---

## 9. 예상 병목 지점 및 대응 방안

t3.small + Docker 5개 컨테이너 구성에서 예상되는 병목 지점입니다.

| 우선순위 | 병목 지점 | 예상 증상 | 확인 방법 | 대응 방안 |
|---------|----------|----------|----------|----------|
| 1 | **HikariCP 커넥션 풀 (기본 10)** | 응답 지연 급증, Connection timeout | `hikaricp_connections_pending > 0` | `spring.datasource.hikari.maximum-pool-size=20~30` |
| 2 | **CPU Credit 고갈** | 전체 응답 시간 급증 (CPU 20% 제한) | CloudWatch CPUCreditBalance = 0 | Unlimited 모드 또는 c5.large로 변경 |
| 3 | **JVM Heap** | OOM, Full GC 빈발 | `jvm_memory_used_bytes > 85%` | `-Xmx512m -Xms512m` 명시 설정 |
| 4 | **Tomcat Thread Pool (기본 200)** | 요청 큐잉, 응답 지연 | `tomcat_threads_busy = max` | `server.tomcat.threads.max=300` |
| 5 | **SSE Emitter 누적** | 메모리 증가, OOM | `SseService.getActiveConnectionCount()` | 사용자당 MAX_EMITTERS 제한 (현재 3) |
| 6 | **Nginx worker_connections** | 502 Bad Gateway | `nginx_connections_active` | `worker_connections` 증가 |

---

## 10. 주의사항

1. **운영 환경에서 테스트 금지:** 반드시 동일 스펙의 별도 테스트 환경에서 수행하거나, 사용자가 없는 시간대에 진행
2. **JMeter GUI 모드 주의:** 100 Thread 이상 시 Listener를 최소화하고, 결과는 `.jtl` 파일로 저장 후 분석
3. **JMeter 클라이언트 스펙:** 부하 생성 측(로컬 PC)도 충분한 리소스 필요 (200 쓰레드 기준 최소 4GB RAM)
4. **쿨다운:** Step 간 3~5분 대기, CPU Credit 회복 확인 후 다음 진행
5. **테스트 데이터 정리:** 테스트 완료 후 생성된 기록/댓글/좋아요 데이터 정리
6. **로그 레벨:** 테스트 중 `debug` 로그가 켜져 있으면 로그 I/O가 병목이 됨 → `WARN`으로 변경
7. **Swap 설정:** t3.small 2GB RAM에 5개 컨테이너는 매우 빡빡함 → 1~2GB swap file 미리 설정 권장

```bash
# EC2에서 swap 설정 (테스트 전)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```
