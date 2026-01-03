BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

TRUNCATE TABLE
    notification,
    friends,
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

-- =========================================================
-- 1) 파라미터 (공평 분배 전제: category_count % user_count = 0, like_target % user_count = 0)
-- =========================================================
DROP TABLE IF EXISTS tmp_params;
CREATE TEMP TABLE tmp_params (
    user_count      int NOT NULL,
    category_count  int NOT NULL,
    like_target     int NOT NULL,
    friend_target   int NOT NULL
) ON COMMIT DROP;

-- users=50, categories=500(=10/user), likes=8000(=160/user), friends=120(=2~3/user)
INSERT INTO tmp_params(user_count, category_count, like_target, friend_target)
VALUES (50, 500, 8000, 120);

-- =========================================================
-- 2) users (rn 1..N 보장)
-- =========================================================
DROP TABLE IF EXISTS tmp_users;
CREATE TEMP TABLE tmp_users (
    rn    int PRIMARY KEY,
    id    uuid NOT NULL,
    email varchar(255) NOT NULL
) ON COMMIT DROP;

WITH src AS (
    SELECT gs AS rn
    FROM generate_series(1, (SELECT user_count FROM tmp_params)) gs
),
     ins AS (
INSERT INTO users (
    id, authority, birth_day, birth_month, birth_year,
    description, email, "password", title, username
)
SELECT
    gen_random_uuid(),
    (CASE WHEN rn % 10 = 0 THEN 0 ELSE 1 END)::int2,         -- 10명 중 1명만 authority=0
    (1 + (rn % 28))::int,
    (1 + (rn % 12))::int,
    (1990 + (rn % 15))::int,
    'dummy user desc ' || rn,
    'user' || rn || '@example.com',
    'pw' || rn,
    'title ' || rn,
    'username' || rn
FROM src
         RETURNING id, email
)
INSERT INTO tmp_users(rn, id, email)
SELECT
    s.rn,
    i.id,
    i.email
FROM src s
         JOIN LATERAL (
    SELECT id, email
    FROM ins
    ORDER BY email
    OFFSET (s.rn - 1) LIMIT 1
    ) i ON true;

-- =========================================================
-- 3) category (유저당 정확히 10개 분배, record_type/visibility도 균등 순환)
-- =========================================================
DROP TABLE IF EXISTS tmp_categories;
CREATE TEMP TABLE tmp_categories (
    rn         int PRIMARY KEY,
    id         uuid NOT NULL,
    user_id    uuid NOT NULL,
    record_type varchar(255) NOT NULL,
    title      varchar(255) NOT NULL,
    visibility varchar(255) NOT NULL
) ON COMMIT DROP;

WITH src AS (
    SELECT gs AS rn
    FROM generate_series(1, (SELECT category_count FROM tmp_params)) gs
),
     mapped AS (
         SELECT
             s.rn,
             u.id AS user_id,
             (ARRAY['CHECK','TIME','TEXT','NUMBER','CHECKLIST'])[1 + ((s.rn - 1) % 5)] AS record_type,
    (CASE WHEN s.rn % 2 = 0 THEN 'PUBLIC' ELSE 'PRIVATE' END) AS visibility,
    'category ' || s.rn AS title
FROM src s
    JOIN tmp_users u
ON u.rn = 1 + ((s.rn - 1) % (SELECT user_count FROM tmp_params))
    ),
    ins AS (
INSERT INTO category(id, user_id, description, record_type, title, visibility)
SELECT
    gen_random_uuid(),
    m.user_id,
    'dummy category desc ' || m.rn,
    m.record_type,
    m.title,
    m.visibility
FROM mapped m
    RETURNING id, user_id, record_type, title, visibility
    ),
    numbered AS (
SELECT row_number() OVER (ORDER BY title) AS rn,
    id, user_id, record_type, title, visibility
FROM ins
    )
INSERT INTO tmp_categories(rn, id, user_id, record_type, title, visibility)
SELECT rn, id, user_id, record_type, title, visibility FROM numbered;

-- =========================================================
-- 4) records (카테고리 단위로 동일 개수 생성 = 자연스럽게 유저에게도 공평)
-- =========================================================
INSERT INTO check_record(id, category_id, "date", success)
SELECT
    gen_random_uuid(),
    c.id,
    (current_date - (d-1))::date,
    ((d % 10) <> 0)  -- 10개 중 1개만 false
FROM tmp_categories c
         JOIN LATERAL generate_series(1, 20) d ON true
WHERE c.record_type = 'CHECK';

INSERT INTO time_record(id, category_id, "date", "time")
SELECT
    gen_random_uuid(),
    c.id,
    (current_date - (d-1))::date,
    make_time((d % 24), (d * 7) % 60, (d * 13) % 60)::time(6)
FROM tmp_categories c
         JOIN LATERAL generate_series(1, 20) d ON true
WHERE c.record_type = 'TIME';

INSERT INTO number_record(id, category_id, "date", "number")
SELECT
    gen_random_uuid(),
    c.id,
    (current_date - (d-1))::date,
    ((c.rn % 100) * 10 + d)::float8
FROM tmp_categories c
         JOIN LATERAL generate_series(1, 20) d ON true
WHERE c.record_type = 'NUMBER';

INSERT INTO text_record(id, category_id, "date", title, text)
SELECT
    gen_random_uuid(),
    c.id,
    (current_date - ((gs-1) % 30))::date,
    'text title ' || gs || ' / ' || c.title,
    'dummy text body #' || gs || ' for ' || c.title
FROM tmp_categories c
    JOIN LATERAL generate_series(1, 15) gs ON true
WHERE c.record_type = 'TEXT';

INSERT INTO check_list_record(id, category_id, "date", todo, success)
SELECT
    gen_random_uuid(),
    c.id,
    (current_date - (day-1))::date,
    'todo ' || todo_i || ' / ' || c.title,
    ((todo_i + day) % 5 <> 0)
FROM tmp_categories c
    JOIN LATERAL generate_series(1, 10) day ON true
    JOIN LATERAL generate_series(1, 3) todo_i ON true
WHERE c.record_type = 'CHECKLIST';

-- =========================================================
-- 5) category_like (유저당 정확히 160개, 자기 카테고리 제외, UNIQUE 보장)
-- =========================================================
DROP TABLE IF EXISTS tmp_public_categories;
CREATE TEMP TABLE tmp_public_categories AS
SELECT
    row_number() OVER (ORDER BY rn) AS prn,
    id,
    user_id,
    title
FROM tmp_categories
WHERE visibility = 'PUBLIC';

WITH p AS (
    SELECT (SELECT count(*) FROM tmp_public_categories) AS public_cnt,
           (SELECT user_count FROM tmp_params) AS user_cnt,
           (SELECT like_target FROM tmp_params) AS like_target
),
     likes_per_user AS (
         SELECT (SELECT like_target FROM tmp_params) / (SELECT user_count FROM tmp_params) AS v
     ),
     pairs AS (
         SELECT
             u.id AS user_id,
             -- 각 유저별 "자기 것 제외한 PUBLIC 카테고리"에서 offset으로 고름
             (
                 SELECT pc.id
                 FROM tmp_public_categories pc
                 WHERE pc.user_id <> u.id
                 ORDER BY pc.prn
                 OFFSET (
             ((u.rn - 1) * (SELECT v FROM likes_per_user) + (i - 1))
    %
    (SELECT count(*) FROM tmp_public_categories pc2 WHERE pc2.user_id <> u.id)
    )
    LIMIT 1
    ) AS category_id,
    (now() - ((i - 1) % 30) * interval '1 day') AS created_at
FROM tmp_users u
    JOIN LATERAL generate_series(1, (SELECT v FROM likes_per_user)) i ON true
    )
INSERT INTO category_like(id, user_id, category_id, created_at)
SELECT gen_random_uuid(), user_id, category_id, created_at
FROM pairs
WHERE category_id IS NOT NULL;

-- =========================================================
-- 6) friends (총 120 row: 앞 20명은 3개, 나머지 2개 = 최대한 공평)
-- =========================================================
WITH base AS (
    SELECT
        (SELECT friend_target FROM tmp_params) / (SELECT user_count FROM tmp_params) AS base_n,  -- 2
        (SELECT friend_target FROM tmp_params) % (SELECT user_count FROM tmp_params) AS rem     -- 20
    ),
    pairs AS (
SELECT
    u.id AS requester_id,
    a.id AS addressee_id,
    (ARRAY['PENDING','ACCEPTED','REJECTED','BLOCKED'])[1 + ((k - 1) % 4)] AS status,
    (now() - (k % 60) * interval '1 day') AS created_at
FROM tmp_users u
    JOIN LATERAL generate_series(
    1,
    (SELECT base_n FROM base) + (CASE WHEN u.rn <= (SELECT rem FROM base) THEN 1 ELSE 0 END)
    ) k ON true
    JOIN tmp_users a
    ON a.rn = 1 + ((u.rn - 1 + k) % (SELECT user_count FROM tmp_params))  -- 다음 유저들로 순환
    )
INSERT INTO friends(id, requester_id, addressee_id, status, created_at)
SELECT gen_random_uuid(), requester_id, addressee_id, status, created_at
FROM pairs;

-- =========================================================
-- 7) comments (depth 최대 5, 규칙 기반으로 분산)
-- =========================================================
DROP TABLE IF EXISTS tmp_comments;
CREATE TEMP TABLE tmp_comments (
    seq        int NOT NULL,
    id         uuid PRIMARY KEY,
    category_id uuid NOT NULL,
    user_id    uuid NOT NULL,
    depth      int NOT NULL
) ON COMMIT DROP;

-- depth 1: 카테고리당 3개 (댓글 작성자도 유저에 골고루)
WITH src AS (
    SELECT
        c.rn AS category_rn,
        c.id AS category_id,
        gs AS idx
    FROM tmp_categories c
             JOIN LATERAL generate_series(1, 3) gs ON true
    ),
    ins AS (
INSERT INTO "comment"(id, category_id, parent_id, user_id, "comment", created_at, updated_at)
SELECT
    gen_random_uuid(),
    s.category_id,
    NULL,
    u.id,
    'root comment #' || s.idx || ' on cat ' || s.category_rn,
    (now() - ((s.idx + s.category_rn) % 30) * interval '1 day'),
    NULL
FROM src s
    JOIN tmp_users u
ON u.rn = 1 + ((s.category_rn + s.idx - 1) % (SELECT user_count FROM tmp_params))
    RETURNING id, category_id, user_id
    ),
    numbered AS (
SELECT row_number() OVER (ORDER BY id) AS seq, id, category_id, user_id
FROM ins
    )
INSERT INTO tmp_comments(seq, id, category_id, user_id, depth)
SELECT seq, id, category_id, user_id, 1 FROM numbered;

-- depth 2: 각 depth1에 2개
WITH parents AS (SELECT * FROM tmp_comments WHERE depth = 1),
     ins AS (
INSERT INTO "comment"(id, category_id, parent_id, user_id, "comment", created_at, updated_at)
SELECT
    gen_random_uuid(),
    p.category_id,
    p.id,
    u.id,
    'reply d2 #' || k,
    (now() - ((p.seq + k) % 30) * interval '1 day'),
    NULL
FROM parents p
    JOIN LATERAL generate_series(1, 2) k ON true
    JOIN tmp_users u
    ON u.rn = 1 + ((p.seq + k - 1) % (SELECT user_count FROM tmp_params))
    RETURNING id, category_id, user_id
    ),
    numbered AS (
SELECT row_number() OVER (ORDER BY id) AS seq, id, category_id, user_id
FROM ins
    )
INSERT INTO tmp_comments(seq, id, category_id, user_id, depth)
SELECT seq, id, category_id, user_id, 2 FROM numbered;

-- depth 3: depth2 중 60% (seq % 10 < 6)
WITH parents AS (SELECT * FROM tmp_comments WHERE depth = 2 AND (seq % 10) < 6),
     ins AS (
INSERT INTO "comment"(id, category_id, parent_id, user_id, "comment", created_at, updated_at)
SELECT
    gen_random_uuid(),
    p.category_id,
    p.id,
    u.id,
    'reply d3',
    (now() - (p.seq % 30) * interval '1 day'),
    NULL
FROM parents p
         JOIN tmp_users u
              ON u.rn = 1 + ((p.seq - 1) % (SELECT user_count FROM tmp_params))
    RETURNING id, category_id, user_id
    ),
    numbered AS (
SELECT row_number() OVER (ORDER BY id) AS seq, id, category_id, user_id
FROM ins
    )
INSERT INTO tmp_comments(seq, id, category_id, user_id, depth)
SELECT seq, id, category_id, user_id, 3 FROM numbered;

-- depth 4: depth3 중 35% (seq % 20 < 7)
WITH parents AS (SELECT * FROM tmp_comments WHERE depth = 3 AND (seq % 20) < 7),
     ins AS (
INSERT INTO "comment"(id, category_id, parent_id, user_id, "comment", created_at, updated_at)
SELECT
    gen_random_uuid(),
    p.category_id,
    p.id,
    u.id,
    'reply d4',
    (now() - (p.seq % 30) * interval '1 day'),
    NULL
FROM parents p
         JOIN tmp_users u
              ON u.rn = 1 + ((p.seq + 3) % (SELECT user_count FROM tmp_params))
    RETURNING id, category_id, user_id
    ),
    numbered AS (
SELECT row_number() OVER (ORDER BY id) AS seq, id, category_id, user_id
FROM ins
    )
INSERT INTO tmp_comments(seq, id, category_id, user_id, depth)
SELECT seq, id, category_id, user_id, 4 FROM numbered;

-- depth 5: depth4 중 20% (seq % 10 < 2)
WITH parents AS (SELECT * FROM tmp_comments WHERE depth = 4 AND (seq % 10) < 2),
     ins AS (
INSERT INTO "comment"(id, category_id, parent_id, user_id, "comment", created_at, updated_at)
SELECT
    gen_random_uuid(),
    p.category_id,
    p.id,
    u.id,
    'reply d5',
    (now() - (p.seq % 30) * interval '1 day'),
    NULL
FROM parents p
         JOIN tmp_users u
              ON u.rn = 1 + ((p.seq + 7) % (SELECT user_count FROM tmp_params))
    RETURNING id, category_id, user_id
    ),
    numbered AS (
SELECT row_number() OVER (ORDER BY id) AS seq, id, category_id, user_id
FROM ins
    )
INSERT INTO tmp_comments(seq, id, category_id, user_id, depth)
SELECT seq, id, category_id, user_id, 5 FROM numbered;

-- =========================================================
-- 8) notifications (LIKE/COMMENT/REPLY)
-- =========================================================
INSERT INTO notification(id, receiver_id, sender_id, "type", "data", "read", created_at)
SELECT
    gen_random_uuid(),
    c.user_id AS receiver_id,
    cl.user_id AS sender_id,
    'LIKE',
    jsonb_build_object('senderEmail', su.email, 'categoryTitle', c.title),
    ((cl.user_id::text > c.user_id::text)),  -- 규칙적으로 read 분산
    COALESCE(cl.created_at, now())
FROM category_like cl
         JOIN category c ON c.id = cl.category_id
         JOIN users su ON su.id = cl.user_id
WHERE c.user_id <> cl.user_id;

INSERT INTO notification(id, receiver_id, sender_id, "type", "data", "read", created_at)
SELECT
    gen_random_uuid(),
    c.user_id AS receiver_id,
    cm.user_id AS sender_id,
    'COMMENT',
    jsonb_build_object('senderEmail', su.email, 'comment', cm."comment", 'categoryTitle', c.title),
    (cm.created_at::text < now()::text),
    cm.created_at
FROM "comment" cm
         JOIN category c ON c.id = cm.category_id
         JOIN users su ON su.id = cm.user_id
WHERE cm.parent_id IS NULL
  AND c.user_id <> cm.user_id;

INSERT INTO notification(id, receiver_id, sender_id, "type", "data", "read", created_at)
SELECT
    gen_random_uuid(),
    parent.user_id AS receiver_id,
    child.user_id AS sender_id,
    'REPLY',
    jsonb_build_object('senderEmail', su.email, 'comment', child."comment"),
    (child.created_at::text < now()::text),
    child.created_at
FROM "comment" child
         JOIN "comment" parent ON parent.id = child.parent_id
         JOIN users su ON su.id = child.user_id
WHERE parent.user_id <> child.user_id;

UPDATE notification SET "read" = false;


COMMIT;
