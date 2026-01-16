BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

TRUNCATE TABLE
    category_like,
    "comment",
    check_list_record,
    check_record,
    number_record,
    text_record,
    time_record,
    category,
    users
CASCADE;

DROP TABLE IF EXISTS tmp_users;
DROP TABLE IF EXISTS tmp_categories;

CREATE TEMP TABLE tmp_users (id UUID PRIMARY KEY);
CREATE TEMP TABLE tmp_categories (id UUID PRIMARY KEY, record_type TEXT NOT NULL);

-- =========================================================
-- 1) users 10명
-- =========================================================
WITH ins AS (
INSERT INTO users (id, authority, birth_day, birth_month, birth_year, description, email, password, title, username)
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
    'user' || lpad(gs::text, 2, '0')
FROM generate_series(1, 10) gs
    RETURNING id
)
INSERT INTO tmp_users(id)
SELECT id FROM ins;

-- =========================================================
-- 2) category 500개 (record_type 5종 균등)
-- ✅ updated_at 제거, created_at만 채움
-- =========================================================
WITH u AS (
    SELECT array_agg(id ORDER BY id) AS ids, count(*) AS n
    FROM tmp_users
),
     ins AS (
INSERT INTO category (
    id, user_id, description, record_type, title, visibility,
    created_at
)
SELECT
    gen_random_uuid(),
    (u.ids)[(((gs - 1) % u.n) + 1)] AS user_id,
        '더미 카테고리 설명 ' || gs,
        CASE (gs % 5)
          WHEN 0 THEN 'CHECK'
          WHEN 1 THEN 'CHECKLIST'
          WHEN 2 THEN 'TEXT'
          WHEN 3 THEN 'TIME'
          ELSE 'NUMBER'
END AS record_type,
        CASE (gs % 5)
          WHEN 0 THEN '습관체크-' || gs
          WHEN 1 THEN '할일-' || gs
          WHEN 2 THEN '일기-' || gs
          WHEN 3 THEN '시간기록-' || gs
          ELSE '숫자기록-' || gs
END AS title,
        CASE WHEN random() < 0.6 THEN 'PUBLIC' ELSE 'PRIVATE' END AS visibility,
        ts
    FROM generate_series(1, 500) gs, u
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
-- 3.5) comment 더미데이터 (최대 5단)
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
           ts,
           ts
FROM generate_series(1, 6000) gs, u, c,
    LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level1(id, category_id, depth)
SELECT id, category_id, 1 FROM ins;

-- depth=2 (70% / 1~4)
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d2) - ' || p.id || '-' || k,
    p.id,
    p.category_id,
    (u.uids)[floor(random()*array_length(u.uids,1))::int + 1],
           ts,
           ts
FROM tmp_level1 p
    CROSS JOIN u
    CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.70 THEN (floor(random()*4)::int + 1) ELSE 0 END AS cnt) r
    CROSS JOIN LATERAL generate_series(1, r.cnt) k
    CROSS JOIN LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level2(id, category_id, depth)
SELECT id, category_id, 2 FROM ins;

-- depth=3 (50% / 1~3)
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d3) - ' || p.id || '-' || k,
    p.id,
    p.category_id,
    (u.uids)[floor(random()*array_length(u.uids,1))::int + 1],
           ts,
           ts
FROM tmp_level2 p
    CROSS JOIN u
    CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.50 THEN (floor(random()*3)::int + 1) ELSE 0 END AS cnt) r
    CROSS JOIN LATERAL generate_series(1, r.cnt) k
    CROSS JOIN LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level3(id, category_id, depth)
SELECT id, category_id, 3 FROM ins;

-- depth=4 (35% / 1~2)
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d4) - ' || p.id || '-' || k,
    p.id,
    p.category_id,
    (u.uids)[floor(random()*array_length(uids,1))::int + 1],
           ts,
           ts
FROM tmp_level3 p
    CROSS JOIN u
    CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.35 THEN (floor(random()*2)::int + 1) ELSE 0 END AS cnt) r
    CROSS JOIN LATERAL generate_series(1, r.cnt) k
    CROSS JOIN LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level4(id, category_id, depth)
SELECT id, category_id, 4 FROM ins;

-- depth=5 (20% / 1)
WITH u AS (SELECT array_agg(id) AS uids FROM tmp_users),
     ins AS (
INSERT INTO "comment" (id, comment, parent_id, category_id, user_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    '댓글(d5) - ' || p.id || '-1',
    p.id,
    p.category_id,
    (u.uids)[floor(random()*array_length(u.uids,1))::int + 1],
           ts,
           ts
FROM tmp_level4 p
    CROSS JOIN u
    CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.20 THEN 1 ELSE 0 END AS cnt) r
    CROSS JOIN LATERAL generate_series(1, r.cnt) k
    CROSS JOIN LATERAL (SELECT now() - (floor(random() * 60 * 24 * 60)::int * interval '1 minute') AS ts) t
    RETURNING id, category_id
    )
INSERT INTO tmp_level5(id, category_id, depth)
SELECT id, category_id, 5 FROM ins;

-- =========================================================
-- 4) check_record: CHECK 카테고리당 최근 120일
-- =========================================================
INSERT INTO check_record (id, date, success, category_id)
SELECT gen_random_uuid(), (current_date - d)::date, (random() < 0.75), cat.id
FROM (SELECT id FROM tmp_categories WHERE record_type = 'CHECK') cat
         CROSS JOIN generate_series(0, 119) d;

-- =========================================================
-- 5) check_list_record: CHECKLIST 카테고리당 최근 60일 * 하루 4개
-- =========================================================
INSERT INTO check_list_record (id, date, success, category_id, todo)
SELECT gen_random_uuid(), (current_date - d)::date, (random() < 0.6), cat.id, 'TODO-' || d || '-' || t
FROM (SELECT id FROM tmp_categories WHERE record_type = 'CHECKLIST') cat
         CROSS JOIN generate_series(0, 59) d
         CROSS JOIN generate_series(1, 4) t;

-- =========================================================
-- 6) text_record: TEXT 카테고리당 최근 90일
-- =========================================================
INSERT INTO text_record (id, date, category_id, text, title)
SELECT gen_random_uuid(), (current_date - d)::date, cat.id, '더미 텍스트 내용 - day ' || d, '제목-' || d
FROM (SELECT id FROM tmp_categories WHERE record_type = 'TEXT') cat
         CROSS JOIN generate_series(0, 89) d;

-- =========================================================
-- 7) time_record: TIME 카테고리당 최근 90일 (0~6시간 랜덤)
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
-- 8) number_record: NUMBER 카테고리당 최근 120일 (0~100)
-- =========================================================
INSERT INTO number_record (id, date, number, category_id)
SELECT gen_random_uuid(), (current_date - d)::date, (floor(random()*101))::int, cat.id
FROM (SELECT id FROM tmp_categories WHERE record_type = 'NUMBER') cat
         CROSS JOIN generate_series(0, 119) d;

-- 검증
SELECT 'users' AS tbl, count(*) FROM users
UNION ALL SELECT 'category', count(*) FROM category
UNION ALL SELECT 'category_like', count(*) FROM category_like
UNION ALL SELECT 'comment', count(*) FROM "comment"
UNION ALL SELECT 'check_record', count(*) FROM check_record
UNION ALL SELECT 'check_list_record', count(*) FROM check_list_record
UNION ALL SELECT 'text_record', count(*) FROM text_record
UNION ALL SELECT 'time_record', count(*) FROM time_record
UNION ALL SELECT 'number_record', count(*) FROM number_record;

COMMIT;
