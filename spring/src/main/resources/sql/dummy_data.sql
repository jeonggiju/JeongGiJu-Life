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
-- 13) chat_room 5개 (친구 관계인 유저 쌍)
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

-- =========================================================
-- 14) chat_message 대화 내용
-- =========================================================

-- 채팅방 1: user1 ↔ user2 (활발한 대화)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY['안녕! 오늘 뭐 했어?', '나는 운동하고 왔어 ㅋㅋ', '오 대단하다! 나도 해야 하는데',
           '같이 하자! 내일 시간 돼?', '내일은 좀 바쁘고 모레는 괜찮아', '그래 모레 저녁에 보자',
           '좋아! 그때 봐 👍', '응응 기대된다!'])[((gs - 1) % 8) + 1],
    CASE WHEN gs <= 12 THEN true ELSE false END,
    NOW() - INTERVAL '3 days' + (gs * INTERVAL '15 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 16) gs WHERE cr.rn = 1;

-- 채팅방 2: user1 ↔ user3 (프로젝트)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY['프로젝트 진행 어때?', '거의 다 했어! 내일 공유할게', '좋아 기대된다',
           '혹시 디자인 쪽 의견 있어?', '깔끔하게 가는 게 좋을 것 같아', '알겠어 참고할게!'])[((gs - 1) % 6) + 1],
    CASE WHEN gs <= 8 THEN true ELSE false END,
    NOW() - INTERVAL '5 hours' + (gs * INTERVAL '20 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 10) gs WHERE cr.rn = 2;

-- 채팅방 3: user1 ↔ user4 (약속)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY['주말에 뭐 할 거야?', '아직 계획 없어, 왜?', '같이 밥 먹자!', '좋아 어디서 볼까?'])[((gs - 1) % 4) + 1],
    true,
    NOW() - INTERVAL '1 day' + (gs * INTERVAL '30 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 6) gs WHERE cr.rn = 3;

-- 채팅방 4: user2 ↔ user3 (책 추천)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY['저번에 알려준 책 다 읽었어', '어땠어? 재밌었어?', '진짜 좋았어! 추천 고마워',
           '다행이다 ㅎㅎ 다음 책도 추천해줄까?', '응 부탁해!'])[((gs - 1) % 5) + 1],
    CASE WHEN gs <= 6 THEN true ELSE false END,
    NOW() - INTERVAL '8 hours' + (gs * INTERVAL '25 minutes')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 8) gs WHERE cr.rn = 4;

-- 채팅방 5: user3 ↔ user6 (안부)
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT gen_random_uuid(), cr.id,
    CASE WHEN gs % 2 = 1 THEN cr.user1_id ELSE cr.user2_id END,
    (ARRAY['요즘 어떻게 지내?', '바쁘지만 잘 지내고 있어!', '나중에 시간 되면 만나자', '그래 꼭!'])[((gs - 1) % 4) + 1],
    true,
    NOW() - INTERVAL '3 days' + (gs * INTERVAL '1 hour')
FROM tmp_chat_rooms cr CROSS JOIN generate_series(1, 4) gs WHERE cr.rn = 5;

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
