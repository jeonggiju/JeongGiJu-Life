-- =========================================================
-- Keep4Life 통합 더미 데이터
-- 전제: ddl-auto: update로 테이블이 자동 생성된 후 실행
-- 실행: psql -U app -d appdb -f dummy_data.sql
-- =========================================================

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 전체 초기화
TRUNCATE TABLE
    chat_message,
    chat_room,
    friendship,
    notification,
    category_like,
    "comment",
    expense_record_tag,
    expense_record,
    expense_tag,
    check_list_record,
    check_record,
    number_record,
    text_record_image,
    text_record,
    time_record,
    category,
    users
CASCADE;

DROP TABLE IF EXISTS tmp_users;
DROP TABLE IF EXISTS tmp_categories;

CREATE TEMP TABLE tmp_users (id UUID PRIMARY KEY, rn INT);
CREATE TEMP TABLE tmp_categories (id UUID PRIMARY KEY, record_type TEXT NOT NULL);

-- =========================================================
-- 1) users 10명
-- =========================================================
WITH ins AS (
INSERT INTO users (id, authority, birth_day, birth_month, birth_year, description, email, password, title, username, nickname, profile_image_url)
SELECT
    gen_random_uuid(),
    1,
    (floor(random() * 28) + 1)::int,
    (floor(random() * 12) + 1)::int,
    (floor(random() * 10) + 1995)::int,
    '더미 사용자 ' || gs,
    'user' || lpad(gs::text, 2, '0') || '@example.com',
    'pass1234',
    'USER',
    'user' || lpad(gs::text, 2, '0'),
    'nickname' || lpad(gs::text, 2, '0'),
    NULL
FROM generate_series(1, 10) gs
    RETURNING id
)
INSERT INTO tmp_users(id, rn)
SELECT id, ROW_NUMBER() OVER (ORDER BY id) FROM ins;

-- =========================================================
-- 2) category 600개 (record_type 6종 균등)
-- =========================================================
WITH u AS (
    SELECT array_agg(id ORDER BY id) AS ids, count(*) AS n
    FROM tmp_users
),
     ins AS (
INSERT INTO category (
    id, user_id, description, record_type, title, visibility, created_at
)
SELECT
    gen_random_uuid(),
    (u.ids)[(((gs - 1) % u.n) + 1)] AS user_id,
        '더미 카테고리 설명 ' || gs,
        CASE (gs % 6)
          WHEN 0 THEN 'CHECK'
          WHEN 1 THEN 'CHECKLIST'
          WHEN 2 THEN 'TEXT'
          WHEN 3 THEN 'TIME'
          WHEN 4 THEN 'NUMBER'
          ELSE 'EXPENSE'
END AS record_type,
        CASE (gs % 6)
          WHEN 0 THEN '습관체크-' || gs
          WHEN 1 THEN '할일-' || gs
          WHEN 2 THEN '일기-' || gs
          WHEN 3 THEN '시간기록-' || gs
          WHEN 4 THEN '숫자기록-' || gs
          ELSE '가계부-' || gs
END AS title,
        CASE WHEN random() < 0.6 THEN 'PUBLIC' ELSE 'PRIVATE' END AS visibility,
        ts
    FROM generate_series(1, 600) gs, u
    CROSS JOIN LATERAL (
        SELECT now() - (floor(random()*90)::int * interval '1 day') AS ts
    ) t
    RETURNING id, record_type
)
INSERT INTO tmp_categories(id, record_type)
SELECT id, record_type FROM ins;

-- =========================================================
-- 3) category_like 8000개
-- =========================================================
WITH u AS (SELECT array_agg(id) AS ids FROM tmp_users),
     c AS (SELECT array_agg(id) AS ids FROM tmp_categories),
     pairs AS (
         SELECT
             gen_random_uuid() AS id,
             now() - (floor(random()*90)::int * interval '1 day') AS created_at,
             (c.ids)[floor(random()*array_length(c.ids,1))::int + 1] AS category_id,
    (u.ids)[floor(random()*array_length(u.ids,1))::int + 1] AS user_id
FROM generate_series(1, 8000), u, c
    )
INSERT INTO category_like (id, created_at, category_id, user_id)
SELECT id, created_at, category_id, user_id
FROM pairs
    ON CONFLICT (user_id, category_id) DO NOTHING;

-- =========================================================
-- 4) comment 더미데이터 (최대 5단)
-- =========================================================
DROP TABLE IF EXISTS tmp_level1;
DROP TABLE IF EXISTS tmp_level2;
DROP TABLE IF EXISTS tmp_level3;
DROP TABLE IF EXISTS tmp_level4;
DROP TABLE IF EXISTS tmp_level5;

CREATE TEMP TABLE tmp_level1 (id UUID PRIMARY KEY, category_id UUID NOT NULL, depth int NOT NULL);
CREATE TEMP TABLE tmp_level2 (id UUID PRIMARY KEY, category_id UUID NOT NULL, depth int NOT NULL);
CREATE TEMP TABLE tmp_level3 (id UUID PRIMARY KEY, category_id UUID NOT NULL, depth int NOT NULL);
CREATE TEMP TABLE tmp_level4 (id UUID PRIMARY KEY, category_id UUID NOT NULL, depth int NOT NULL);
CREATE TEMP TABLE tmp_level5 (id UUID PRIMARY KEY, category_id UUID NOT NULL, depth int NOT NULL);

-- depth=1 루트 6000
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     c AS (SELECT array_agg(id) AS cids FROM tmp_categories),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d1) - ' || gs,
    NULL::uuid,
    (c.cids)[floor(random()*array_length(c.cids,1))::int + 1],
           (u.uids)[floor(random()*array_length(u.uids,1))::int + 1],
           ts, ts
FROM generate_series(1, 6000) gs, u, c,
    LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level1(id, category_id, depth) SELECT id, category_id, 1 FROM ins;

-- depth=2 (70% / 1~4)
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d2) - ' || p.id || '-' || k,
    p.id, p.category_id,
    (u.uids)[floor(random()*array_length(u.uids,1))::int + 1],
           ts, ts
FROM tmp_level1 p CROSS JOIN u
    CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.70 THEN (floor(random()*4)::int + 1) ELSE 0 END AS cnt) r
    CROSS JOIN LATERAL generate_series(1, r.cnt) k
    CROSS JOIN LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level2(id, category_id, depth) SELECT id, category_id, 2 FROM ins;

-- depth=3 (50% / 1~3)
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d3) - ' || p.id || '-' || k,
    p.id, p.category_id,
    (u.uids)[floor(random()*array_length(u.uids,1))::int + 1],
           ts, ts
FROM tmp_level2 p CROSS JOIN u
    CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.50 THEN (floor(random()*3)::int + 1) ELSE 0 END AS cnt) r
    CROSS JOIN LATERAL generate_series(1, r.cnt) k
    CROSS JOIN LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level3(id, category_id, depth) SELECT id, category_id, 3 FROM ins;

-- depth=4 (35% / 1~2)
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d4) - ' || p.id || '-' || k,
    p.id, p.category_id,
    (u.uids)[floor(random()*array_length(uids,1))::int + 1],
           ts, ts
FROM tmp_level3 p CROSS JOIN u
    CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.35 THEN (floor(random()*2)::int + 1) ELSE 0 END AS cnt) r
    CROSS JOIN LATERAL generate_series(1, r.cnt) k
    CROSS JOIN LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level4(id, category_id, depth) SELECT id, category_id, 4 FROM ins;

-- depth=5 (20% / 1)
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d5) - ' || p.id || '-1',
    p.id, p.category_id,
    (u.uids)[floor(random()*array_length(u.uids,1))::int + 1],
           ts, ts
FROM tmp_level4 p CROSS JOIN u
    CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.20 THEN 1 ELSE 0 END AS cnt) r
    CROSS JOIN LATERAL generate_series(1, r.cnt) k
    CROSS JOIN LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level5(id, category_id, depth) SELECT id, category_id, 5 FROM ins;

-- =========================================================
-- 5) check_record: CHECK 카테고리당 최근 120일
-- =========================================================
INSERT INTO check_record (id, date, success, category_id)
SELECT gen_random_uuid(), (current_date - d)::date, (random() < 0.75), cat.id
FROM (SELECT id FROM tmp_categories WHERE record_type = 'CHECK') cat
         CROSS JOIN generate_series(0, 119) d;

-- =========================================================
-- 6) check_list_record: CHECKLIST 카테고리당 최근 60일 * 하루 4개
-- =========================================================
INSERT INTO check_list_record (id, date, success, category_id, todo)
SELECT gen_random_uuid(), (current_date - d)::date, (random() < 0.6), cat.id, 'TODO-' || d || '-' || t
FROM (SELECT id FROM tmp_categories WHERE record_type = 'CHECKLIST') cat
         CROSS JOIN generate_series(0, 59) d
         CROSS JOIN generate_series(1, 4) t;

-- =========================================================
-- 7) text_record: TEXT 카테고리당 최근 90일
-- =========================================================
INSERT INTO text_record (id, date, category_id, text, title)
SELECT gen_random_uuid(), (current_date - d)::date, cat.id, '더미 텍스트 내용 - day ' || d, '제목-' || d
FROM (SELECT id FROM tmp_categories WHERE record_type = 'TEXT') cat
         CROSS JOIN generate_series(0, 89) d;

-- =========================================================
-- 8) time_record: TIME 카테고리당 최근 90일 (0~6시간 랜덤)
-- =========================================================
INSERT INTO time_record (id, date, time, category_id)
SELECT
    gen_random_uuid(),
    (current_date - d)::date,
    (time '00:00:00' + ((floor(random()* (6*3600))::int) * interval '1 second'))::time,
    cat.id
FROM (SELECT id FROM tmp_categories WHERE record_type = 'TIME') cat
    CROSS JOIN generate_series(0, 89) d;

-- =========================================================
-- 9) number_record: NUMBER 카테고리당 최근 120일 (0~100)
-- =========================================================
INSERT INTO number_record (id, date, number, category_id)
SELECT gen_random_uuid(), (current_date - d)::date, (floor(random()*101))::int, cat.id
FROM (SELECT id FROM tmp_categories WHERE record_type = 'NUMBER') cat
         CROSS JOIN generate_series(0, 119) d;

-- =========================================================
-- 10) expense_tag: 유저별 태그 생성
-- =========================================================
INSERT INTO expense_tag (id, name, user_id)
SELECT gen_random_uuid(), tag_name, u.id
FROM tmp_users u
CROSS JOIN (VALUES ('식비'), ('교통'), ('쇼핑'), ('의료'), ('여가'), ('구독'), ('기타')) AS tags(tag_name);

-- =========================================================
-- 11) expense_record: EXPENSE 카테고리당 최근 90일 (하루 0~3건)
-- =========================================================
INSERT INTO expense_record (id, amount, expense_type, payment_method, merchant, memo, date, category_id)
SELECT
    gen_random_uuid(),
    round((random() * 50000 + 1000)::numeric, 0),
    (ARRAY['INCOME', 'EXPENSE', 'EXPENSE', 'EXPENSE'])[floor(random() * 4 + 1)],
    (ARRAY['CASH', 'CARD', 'TRANSFER', 'OTHER'])[floor(random() * 4 + 1)],
    (ARRAY['편의점', '카페', '마트', '식당', '주유소', '병원', '온라인쇼핑', '교통', '구독'])[floor(random() * 9 + 1)],
    '메모 ' || d || '-' || t,
    (current_date - d)::date,
    cat.id
FROM (SELECT id FROM tmp_categories WHERE record_type = 'EXPENSE') cat
    CROSS JOIN generate_series(0, 89) d
    CROSS JOIN generate_series(1, (floor(random() * 3 + 1))::int) t;

-- =========================================================
-- 12) friendship 데이터
--     ACCEPTED: user1↔user2~5, user2↔user3, user2↔user4, user3↔user6, user4↔user7
--     PENDING:  user6→user1, user7→user2, user8→user3
--     REJECTED: user8→user1
-- =========================================================

-- ACCEPTED: user1 ↔ user2~5
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
SELECT
    gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 1),
    (SELECT id FROM tmp_users WHERE rn = gs),
    'ACCEPTED',
    NOW() - INTERVAL '30 days' + (gs * INTERVAL '1 day'),
    NOW() - INTERVAL '29 days' + (gs * INTERVAL '1 day')
FROM generate_series(2, 5) gs;

-- ACCEPTED: user2 ↔ user3
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 2), (SELECT id FROM tmp_users WHERE rn = 3),
    'ACCEPTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '19 days');

-- ACCEPTED: user2 ↔ user4
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 2), (SELECT id FROM tmp_users WHERE rn = 4),
    'ACCEPTED', NOW() - INTERVAL '18 days', NOW() - INTERVAL '17 days');

-- ACCEPTED: user3 ↔ user6
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 3), (SELECT id FROM tmp_users WHERE rn = 6),
    'ACCEPTED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days');

-- ACCEPTED: user4 ↔ user7
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 4), (SELECT id FROM tmp_users WHERE rn = 7),
    'ACCEPTED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days');

-- PENDING: user6 → user1
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 6), (SELECT id FROM tmp_users WHERE rn = 1),
    'PENDING', NOW() - INTERVAL '2 days', NULL);

-- PENDING: user7 → user2
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 7), (SELECT id FROM tmp_users WHERE rn = 2),
    'PENDING', NOW() - INTERVAL '1 day', NULL);

-- PENDING: user8 → user3
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 8), (SELECT id FROM tmp_users WHERE rn = 3),
    'PENDING', NOW() - INTERVAL '3 hours', NULL);

-- REJECTED: user8 → user1
INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 8), (SELECT id FROM tmp_users WHERE rn = 1),
    'REJECTED', NOW() - INTERVAL '25 days', NOW() - INTERVAL '24 days');

-- =========================================================
-- 13) chat_room 8개 (친구 관계인 유저 쌍 전부)
-- =========================================================
DROP TABLE IF EXISTS tmp_chat_rooms;
CREATE TEMP TABLE tmp_chat_rooms (id UUID PRIMARY KEY, user1_id UUID, user2_id UUID, rn INT);

-- user1 ↔ user2
WITH ins AS (
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 1), (SELECT id FROM tmp_users WHERE rn = 2),
    NOW() - INTERVAL '28 days', NOW() - INTERVAL '10 minutes')
    RETURNING id, user1_id, user2_id
)
INSERT INTO tmp_chat_rooms SELECT id, user1_id, user2_id, 1 FROM ins;

-- user1 ↔ user3
WITH ins AS (
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 1), (SELECT id FROM tmp_users WHERE rn = 3),
    NOW() - INTERVAL '25 days', NOW() - INTERVAL '2 hours')
    RETURNING id, user1_id, user2_id
)
INSERT INTO tmp_chat_rooms SELECT id, user1_id, user2_id, 2 FROM ins;

-- user1 ↔ user4
WITH ins AS (
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 1), (SELECT id FROM tmp_users WHERE rn = 4),
    NOW() - INTERVAL '20 days', NOW() - INTERVAL '1 day')
    RETURNING id, user1_id, user2_id
)
INSERT INTO tmp_chat_rooms SELECT id, user1_id, user2_id, 3 FROM ins;

-- user2 ↔ user3
WITH ins AS (
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 2), (SELECT id FROM tmp_users WHERE rn = 3),
    NOW() - INTERVAL '18 days', NOW() - INTERVAL '5 hours')
    RETURNING id, user1_id, user2_id
)
INSERT INTO tmp_chat_rooms SELECT id, user1_id, user2_id, 4 FROM ins;

-- user3 ↔ user6
WITH ins AS (
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 3), (SELECT id FROM tmp_users WHERE rn = 6),
    NOW() - INTERVAL '12 days', NOW() - INTERVAL '3 days')
    RETURNING id, user1_id, user2_id
)
INSERT INTO tmp_chat_rooms SELECT id, user1_id, user2_id, 5 FROM ins;

-- user1 ↔ user5
WITH ins AS (
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 1), (SELECT id FROM tmp_users WHERE rn = 5),
    NOW() - INTERVAL '15 days', NOW() - INTERVAL '30 minutes')
    RETURNING id, user1_id, user2_id
)
INSERT INTO tmp_chat_rooms SELECT id, user1_id, user2_id, 6 FROM ins;

-- user2 ↔ user4
WITH ins AS (
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 2), (SELECT id FROM tmp_users WHERE rn = 4),
    NOW() - INTERVAL '14 days', NOW() - INTERVAL '4 hours')
    RETURNING id, user1_id, user2_id
)
INSERT INTO tmp_chat_rooms SELECT id, user1_id, user2_id, 7 FROM ins;

-- user4 ↔ user7
WITH ins AS (
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
VALUES (gen_random_uuid(),
    (SELECT id FROM tmp_users WHERE rn = 4), (SELECT id FROM tmp_users WHERE rn = 7),
    NOW() - INTERVAL '8 days', NOW() - INTERVAL '6 hours')
    RETURNING id, user1_id, user2_id
)
INSERT INTO tmp_chat_rooms SELECT id, user1_id, user2_id, 8 FROM ins;

-- =========================================================
-- 14) chat_message 대화 내용
-- =========================================================

-- 채팅방 1: user1 ↔ user2 (활발한 일상 대화, 30개)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY[
        '안녕! 오늘 뭐 했어?',
        '나는 운동하고 왔어 ㅋㅋ',
        '오 대단하다! 나도 해야 하는데',
        '같이 하자! 내일 시간 돼?',
        '내일은 좀 바쁘고 모레는 괜찮아',
        '그래 모레 저녁에 보자',
        '좋아! 그때 봐',
        '응응 기대된다!',
        '아 참 오늘 점심에 새로 생긴 파스타집 갔는데 대박이었어',
        '어디? 나도 가보고 싶다',
        '역삼역 3번 출구 쪽이야 이름이 뭐더라... 파스타로드?',
        '오 검색해볼게 고마워!',
        '근데 요즘 넷플릭스에 볼 거 있어?',
        '어제 새로 나온 드라마 봤는데 괜찮더라',
        '무슨 드라마? 추천해줘',
        '제목이 기억이 안 나는데 찾아서 보내줄게',
        'ㅋㅋ 빨리 보내줘',
        '찾았다! 링크 보낼게',
        '오 고마워 오늘 저녁에 볼게',
        '재밌으면 알려줘 ㅋㅋ',
        '아 그리고 다음 주 금요일에 모임 있대',
        '진짜? 누가 주최하는 건데?',
        '민수가 한다고 장소는 강남 쪽이래',
        '오 좋다 나도 갈게!',
        '그럼 같이 가자 내가 차 끌고 갈게',
        '역시 든든하다 ㅋㅋ',
        '몇 시쯤 출발하면 될까?',
        '6시쯤?',
        '그래 6시에 보자!',
        '알겠어 그때 봐!'
    ])[((gs - 1) % 30) + 1],
    CASE WHEN gs <= 24 THEN true ELSE false END,
    NOW() - INTERVAL '3 days' + (gs * INTERVAL '12 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 30) gs WHERE cr.rn = 1;

-- 채팅방 2: user1 ↔ user3 (프로젝트 협업, 24개)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY[
        '프로젝트 진행 어때?',
        '거의 다 했어! 내일 공유할게',
        '좋아 기대된다',
        '혹시 디자인 쪽 의견 있어?',
        '깔끔하게 가는 게 좋을 것 같아',
        '알겠어 참고할게!',
        'API 명세서 정리했는데 한번 봐줄 수 있어?',
        '응 지금 보내줘',
        '노션에 올려놨어 링크 확인해봐',
        '봤어! 전체적으로 좋은데 인증 쪽 수정이 좀 필요할 것 같아',
        '어떤 부분?',
        '토큰 갱신 로직이 빠져있어 추가하면 좋겠어',
        '아 맞다 그거 까먹었네 바로 반영할게',
        '고마워! 그리고 테스트 코드도 좀 추가하자',
        '동의 이번 주 안에 커버리지 70% 이상 맞추자',
        '좋아 나는 서비스 레이어 테스트 할게',
        '그럼 나는 컨트롤러 쪽 할게',
        '완벽해 빨리 끝내고 배포하자',
        '배포는 금요일 목표로 하면 될까?',
        '응 금요일이면 충분할 것 같아',
        'CI/CD 파이프라인은 내가 세팅해놨어',
        '오 벌써? 역시 일 빠르다',
        '아니야 ㅋㅋ 그냥 예전 거 재활용한 거야',
        '어쨌든 고마워 이번 프로젝트 잘 마무리하자!'
    ])[((gs - 1) % 24) + 1],
    CASE WHEN gs <= 18 THEN true ELSE false END,
    NOW() - INTERVAL '5 hours' + (gs * INTERVAL '10 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 24) gs WHERE cr.rn = 2;

-- 채팅방 3: user1 ↔ user4 (약속 잡기, 20개)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY[
        '주말에 뭐 할 거야?',
        '아직 계획 없어, 왜?',
        '같이 밥 먹자!',
        '좋아 어디서 볼까?',
        '홍대 쪽 어때?',
        '홍대 좋지! 뭐 먹을까?',
        '고기 먹고 싶다 ㅋㅋ',
        '오 나도! 삼겹살 ㄱㄱ',
        '좋아 내가 맛집 찾아볼게',
        '응 예약도 해줘 ㅋㅋ',
        '알겠어 토요일 저녁 7시 괜찮아?',
        '완벽해!',
        '예약했어! 홍대역 2번 출구에서 만나자',
        '오키 거기서 봐',
        '아 참 주영이도 같이 간대',
        '오 좋아 오랜만이다',
        '셋이서 신나게 먹자 ㅋㅋ',
        '기대된다 빨리 토요일 왔으면',
        '나도 ㅋㅋ 이번 주 너무 길다',
        '조금만 참자! 곧이야'
    ])[((gs - 1) % 20) + 1],
    CASE WHEN gs <= 16 THEN true ELSE false END,
    NOW() - INTERVAL '1 day' + (gs * INTERVAL '15 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 20) gs WHERE cr.rn = 3;

-- 채팅방 4: user2 ↔ user3 (책 추천 & 스터디, 22개)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY[
        '저번에 알려준 책 다 읽었어',
        '어땠어? 재밌었어?',
        '진짜 좋았어! 추천 고마워',
        '다행이다 ㅎㅎ 다음 책도 추천해줄까?',
        '응 부탁해!',
        '클린 코드 읽어봤어?',
        '아직 안 읽었어 좋아?',
        '개발자 필독서야 꼭 읽어봐',
        '오 바로 주문할게',
        '그리고 같이 스터디 할래?',
        '무슨 스터디?',
        '알고리즘 스터디 주 1회 정도',
        '좋아! 요일은 언제가 좋아?',
        '수요일 저녁 어때?',
        '수요일 괜찮아 온라인으로?',
        '응 디스코드로 하자',
        '좋아 방 만들어줘',
        '만들었어 초대 링크 보낼게',
        '고마워 들어갔어!',
        '그럼 이번 주 수요일부터 시작하자',
        '알겠어 첫 주제는 뭘로 할까?',
        '그래프 탐색부터 하자 BFS/DFS'
    ])[((gs - 1) % 22) + 1],
    CASE WHEN gs <= 16 THEN true ELSE false END,
    NOW() - INTERVAL '8 hours' + (gs * INTERVAL '15 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 22) gs WHERE cr.rn = 4;

-- 채팅방 5: user3 ↔ user6 (안부 & 근황, 18개)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY[
        '요즘 어떻게 지내?',
        '바쁘지만 잘 지내고 있어!',
        '나중에 시간 되면 만나자',
        '그래 꼭!',
        '요즘 회사는 어때?',
        '프로젝트가 좀 바쁜데 나름 재밌어',
        '뭐 만들고 있는데?',
        '모바일 앱 리뉴얼 중이야',
        '오 멋지다! 출시되면 알려줘',
        '응 꼭! 그쪽은 뭐 하고 있어?',
        '나는 이직 준비 중이야',
        '오 진짜? 어디 쪽으로?',
        '백엔드 쪽으로 좀 더 큰 회사 보고 있어',
        '대박 응원한다! 필요한 거 있으면 말해',
        '고마워 면접 팁 좀 알려줘 ㅋㅋ',
        '시스템 디자인이 제일 중요해 거기 집중해봐',
        '알겠어 자료 좀 공유해줄 수 있어?',
        '응 정리해서 보내줄게!'
    ])[((gs - 1) % 18) + 1],
    CASE WHEN gs <= 14 THEN true ELSE false END,
    NOW() - INTERVAL '3 days' + (gs * INTERVAL '20 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 18) gs WHERE cr.rn = 5;

-- 채팅방 6: user1 ↔ user5 (운동 메이트, 26개)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY[
        '오늘 헬스장 갈 거야?',
        '응 7시쯤 갈 생각이야',
        '나도 갈게 같이 하자!',
        '좋아 오늘 하체 하는 날이지?',
        '응 스쿼트 빡세게 하자 ㅋㅋ',
        '아 다리 벌써 아프다',
        'ㅋㅋㅋ 안 하면 더 아파',
        '맞아 가자가자',
        '아 그리고 프로틴 다 떨어졌는데 추천해줄 거 있어?',
        '나 마이프로틴 초코맛 먹는데 괜찮아',
        '맛있어? 뒷맛은?',
        '나쁘지 않아 가성비 최고임',
        '오 그러면 같이 주문하자 배송비 아끼게',
        'ㅋㅋ 좋아 내가 장바구니 공유할게',
        '고마워! 아 근데 오늘 벤치프레스 PR 깰 것 같아',
        '오 지금 얼마야?',
        '80kg인데 오늘 85 도전해볼 거야',
        '대박 나 보조 서줄게!',
        '든든하다 ㅋㅋ',
        '아 그리고 다음 달에 바디프로필 찍을 건데 같이 할래?',
        '바디프로필? 나도 해보고 싶었어!',
        '그럼 같이 준비하자 식단도 맞추고',
        '좋아 월요일부터 시작하자',
        '닭가슴살 질릴 각오해 ㅋㅋ',
        '이미 각오했어 ㅋㅋㅋ',
        '그래 파이팅!'
    ])[((gs - 1) % 26) + 1],
    CASE WHEN gs <= 20 THEN true ELSE false END,
    NOW() - INTERVAL '2 days' + (gs * INTERVAL '10 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 26) gs WHERE cr.rn = 6;

-- 채팅방 7: user2 ↔ user4 (게임 & 취미, 20개)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY[
        '오늘 저녁에 롤 할래?',
        '좋아! 몇 시에?',
        '9시쯤 어때?',
        '오키 칼 접속할게',
        '듀오 가자 요즘 랭크 어때?',
        '골드에서 못 올라가고 있어 ㅠ',
        'ㅋㅋ 내가 캐리해줄게',
        '진짜? 너 플래 갔다며',
        '응 어제 승급했어',
        '부럽다 나도 빨리 올라가고 싶다',
        '서포터로 하면 금방 올라가',
        '서포터 싫어 ㅋㅋ 딜 하고 싶어',
        '그럼 원딜 해 내가 서포터 할게',
        '오 고마워 조합 미쳤다',
        '아 근데 새 패치 봤어?',
        '응 챔피언 밸런스 또 바뀌었더라',
        '내 원챔 버프됐어 ㅋㅋㅋ',
        '럭키 ㅋㅋ 내 캐릭은 너프됐는데',
        '안습이다 ㅋㅋ',
        '어쨌든 오늘 저녁에 보자!'
    ])[((gs - 1) % 20) + 1],
    CASE WHEN gs <= 14 THEN true ELSE false END,
    NOW() - INTERVAL '6 hours' + (gs * INTERVAL '12 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 20) gs WHERE cr.rn = 7;

-- 채팅방 8: user4 ↔ user7 (직장 동료, 16개)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY[
        '오늘 회의 몇 시였지?',
        '2시야 회의실 B',
        '고마워 자료 준비해야 하는데',
        '내가 어제 정리해둔 거 공유해줄까?',
        '아 진짜? 부탁해!',
        '메일로 보냈어 확인해봐',
        '받았어 감사합니다!',
        '별말씀을 ㅋㅋ',
        '점심 뭐 먹을까?',
        '회사 앞에 새로 생긴 국밥집 가볼까?',
        '국밥 좋지 가자!',
        '12시에 로비에서 만나',
        '아 그리고 내일 재택이지?',
        '응 내일 재택이야',
        '그럼 슬랙으로 연락하자',
        '알겠어 내일 봐!'
    ])[((gs - 1) % 16) + 1],
    CASE WHEN gs <= 12 THEN true ELSE false END,
    NOW() - INTERVAL '8 hours' + (gs * INTERVAL '18 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 16) gs WHERE cr.rn = 8;

-- =========================================================
-- 임시 테이블 정리
-- =========================================================
DROP TABLE IF EXISTS tmp_users;
DROP TABLE IF EXISTS tmp_categories;
DROP TABLE IF EXISTS tmp_chat_rooms;
DROP TABLE IF EXISTS tmp_level1;
DROP TABLE IF EXISTS tmp_level2;
DROP TABLE IF EXISTS tmp_level3;
DROP TABLE IF EXISTS tmp_level4;
DROP TABLE IF EXISTS tmp_level5;

COMMIT;

-- =========================================================
-- 결과 확인
-- =========================================================
SELECT 'users' AS "table", count(*) AS count FROM users
UNION ALL SELECT 'category', count(*) FROM category
UNION ALL SELECT 'category_like', count(*) FROM category_like
UNION ALL SELECT 'comment', count(*) FROM "comment"
UNION ALL SELECT 'check_record', count(*) FROM check_record
UNION ALL SELECT 'check_list_record', count(*) FROM check_list_record
UNION ALL SELECT 'text_record', count(*) FROM text_record
UNION ALL SELECT 'time_record', count(*) FROM time_record
UNION ALL SELECT 'number_record', count(*) FROM number_record
UNION ALL SELECT 'expense_record', count(*) FROM expense_record
UNION ALL SELECT 'expense_tag', count(*) FROM expense_tag
UNION ALL SELECT 'friendship', count(*) FROM friendship
UNION ALL SELECT 'chat_room', count(*) FROM chat_room
UNION ALL SELECT 'chat_message', count(*) FROM chat_message;
