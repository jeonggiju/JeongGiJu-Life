-- SQL only script for generating a large volume of dummy data using generate_series.
-- This script is for PostgreSQL and is wrapped in a single DO block to be executed atomically.
-- FINAL CORRECTED VERSION - 2026-01-31

-- Pre-step: Drop stale check constraints BEFORE the DO block
-- (ddl-auto:update doesn't refresh enum check constraints)
ALTER TABLE expense_record DROP CONSTRAINT IF EXISTS expense_record_payment_method_check;
ALTER TABLE expense_record DROP CONSTRAINT IF EXISTS expense_record_expense_type_check;
ALTER TABLE category DROP CONSTRAINT IF EXISTS category_record_type_check;

DO $$
DECLARE
    user_id_val uuid := 'a1b2c3d4-e5f6-7890-1234-567890abcdef';
    text_cat_id uuid := '11111111-1111-1111-1111-111111111111';
    check_cat_id uuid := '22222222-2222-2222-2222-222222222222';
    checklist_cat_id uuid := '33333333-3333-3333-3333-333333333333';
    expense_cat_id uuid := '44444444-4444-4444-4444-444444444444';
    time_cat_id uuid := '55555555-5555-5555-5555-555555555555';
    number_cat_id uuid := '66666666-6666-6666-6666-666666666666';
BEGIN
    -- Ensure uuid-ossp extension is enabled
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

    -- Step 0: Clear existing data to allow multiple runs
    RAISE NOTICE 'Truncating existing data...';
    TRUNCATE TABLE users, category, text_record, check_record, check_list_record, expense_record, time_record, number_record, comment, expense_tag, expense_record_tag, notification, text_record_image RESTART IDENTITY CASCADE;

    -- Step 1: Create a single user
    RAISE NOTICE 'Creating user...';
    INSERT INTO users (id, email, username, nickname, password, profile_image_url, title, description, authority, birth_year, birth_month, birth_day) VALUES
    (user_id_val, 'bulkuser@example.com', 'bulkuser', 'BulkUser', 'password123', 'http://example.com/profile_bulk.jpg', 'Bulk Data User', 'A user for testing large data volumes.', 0, 1990, 1, 1);

    -- Step 2: Create one category for each record type
    RAISE NOTICE 'Creating categories...';
    INSERT INTO category (id, user_id, title, description, record_type, visibility, created_at) VALUES
    (text_cat_id, user_id_val, 'My Daily Journal', 'A journal with many entries.', 'TEXT', 'PRIVATE', NOW()),
    (check_cat_id, user_id_val, 'Daily Habit Tracker', 'Tracking daily habits.', 'CHECK', 'PRIVATE', NOW()),
    (checklist_cat_id, user_id_val, 'Project Task List', 'A long list of project tasks.', 'CHECKLIST', 'PRIVATE', NOW()),
    (expense_cat_id, user_id_val, 'All My Expenses', 'A detailed log of all expenses.', 'EXPENSE', 'PUBLIC', NOW()),
    (time_cat_id, user_id_val, 'Sleep Log', 'Tracking sleep times over the years.', 'TIME', 'PRIVATE', NOW()),
    (number_cat_id, user_id_val, 'Weight Measurements', 'Tracking weight fluctuations.', 'NUMBER', 'PRIVATE', NOW());

    -- Step 3: Generate 1000 records for each category using bulk inserts from generate_series

    -- 1000 TEXT records
    RAISE NOTICE 'Generating 1000 TEXT records...';
    INSERT INTO text_record (id, category_id, title, text, date)
    SELECT
        uuid_generate_v4(),
        text_cat_id,
        'Journal Entry #' || s.i,
        'This is the detailed content for journal entry number ' || s.i || '. The day was quite interesting.',
        ('2026-01-30'::date - s.i * interval '1 day')
    FROM generate_series(1, 1000) AS s(i);

    -- 1000 CHECK records
    RAISE NOTICE 'Generating 1000 CHECK records...';
    INSERT INTO check_record (id, category_id, success, date)
    SELECT
        uuid_generate_v4(),
        check_cat_id,
        random() > 0.5,
        ('2026-01-30'::date - s.i * interval '1 day')
    FROM generate_series(1, 1000) AS s(i);

    -- 1000 CHECK_LIST records
    RAISE NOTICE 'Generating 1000 CHECK_LIST records...';
    INSERT INTO check_list_record (id, category_id, todo, success, date)
    SELECT
        uuid_generate_v4(),
        checklist_cat_id,
        'Task number ' || s.i,
        random() > 0.5,
        ('2026-01-30'::date - floor(s.i / 5.0) * interval '1 day') -- 5 tasks per day
    FROM generate_series(1, 1000) AS s(i);

    -- 1000 EXPENSE records (mix of INCOME and EXPENSE, all payment methods)
    -- 최근 365일 내 랜덤 날짜 → 하루에 0~여러건, 빈 날도 자연스럽게 발생
    RAISE NOTICE 'Generating 1000 EXPENSE records...';
    INSERT INTO expense_record (id, category_id, amount, expense_type, payment_method, merchant, memo, date)
    SELECT
        uuid_generate_v4(),
        expense_cat_id,
        round((random() * 300 + 5)::numeric, 2),
        (ARRAY['INCOME', 'EXPENSE', 'EXPENSE', 'EXPENSE'])[floor(random() * 4 + 1)], -- 지출 75%, 수입 25%
        (ARRAY['CASH', 'CARD', 'TRANSFER', 'OTHER'])[floor(random() * 4 + 1)],
        (ARRAY['편의점', '카페', '마트', '식당', '주유소', '병원', '온라인쇼핑', '교통', '구독료', '기타'])[floor(random() * 10 + 1)],
        'Memo for expense #' || s.i,
        ('2026-01-30'::date - floor(random() * 365)::int)
    FROM generate_series(1, 1000) AS s(i);

    -- 1000 TIME records
    RAISE NOTICE 'Generating 1000 TIME records...';
    INSERT INTO time_record (id, category_id, time, date)
    SELECT
        uuid_generate_v4(),
        time_cat_id,
        time '22:00:00' + (random() * interval '3 hours'),
        ('2026-01-30'::date - s.i * interval '1 day')
    FROM generate_series(1, 1000) AS s(i);

    -- 1000 NUMBER records
    RAISE NOTICE 'Generating 1000 NUMBER records...';
    INSERT INTO number_record (id, category_id, number, date)
    SELECT
        uuid_generate_v4(),
        number_cat_id,
        round((70 + random() * 5 - 2.5)::numeric, 2),
        ('2026-01-30'::date - s.i * interval '1 day')
    FROM generate_series(1, 1000) AS s(i);

    -- Step 4: Comments (각 카테고리당 50개, 총 300개 + 대댓글 100개)
    RAISE NOTICE 'Generating comments...';

    -- 각 카테고리에 루트 댓글 50개씩
    INSERT INTO comment (id, category_id, user_id, comment, parent_id, created_at, updated_at)
    SELECT
        uuid_generate_v4(),
        cat_id,
        user_id_val,
        'Comment #' || s.i || ' on category',
        NULL,
        NOW() - (s.i * interval '2 hours'),
        NOW() - (s.i * interval '2 hours')
    FROM generate_series(1, 50) AS s(i),
    (VALUES (text_cat_id), (check_cat_id), (checklist_cat_id), (expense_cat_id), (time_cat_id), (number_cat_id)) AS cats(cat_id);

    -- 대댓글 100개 (랜덤 루트 댓글에 달기)
    INSERT INTO comment (id, category_id, user_id, comment, parent_id, created_at, updated_at)
    SELECT
        uuid_generate_v4(),
        c.category_id,
        user_id_val,
        'Reply #' || s.i || ' to comment',
        c.id,
        NOW() - (s.i * interval '1 hour'),
        NOW() - (s.i * interval '1 hour')
    FROM generate_series(1, 100) AS s(i)
    CROSS JOIN LATERAL (
        SELECT id, category_id FROM comment WHERE parent_id IS NULL ORDER BY random() LIMIT 1
    ) c;

    -- Step 5: Expense Tags (10개 태그)
    RAISE NOTICE 'Generating expense tags...';
    INSERT INTO expense_tag (id, user_id, name)
    SELECT
        uuid_generate_v4(),
        user_id_val,
        tag_name
    FROM (VALUES ('식비'), ('교통비'), ('쇼핑'), ('의료'), ('교육'), ('여가'), ('통신비'), ('주거비'), ('보험'), ('기타')) AS t(tag_name);

    -- Step 6: Expense Record Tags (각 expense_record에 1~3개 태그 연결)
    RAISE NOTICE 'Generating expense record tags...';
    INSERT INTO expense_record_tag (id, expense_record_id, expense_tag_id)
    SELECT
        uuid_generate_v4(),
        er.id,
        et.id
    FROM expense_record er
    CROSS JOIN LATERAL (
        SELECT id FROM expense_tag WHERE user_id = user_id_val ORDER BY random() LIMIT (floor(random() * 3 + 1)::int)
    ) et;

    -- Step 7: Notifications (200개 - 자기 자신에게 보내는 형태로)
    RAISE NOTICE 'Generating notifications...';
    INSERT INTO notification (id, receiver_id, sender_id, type, data, read, created_at)
    SELECT
        uuid_generate_v4(),
        user_id_val,
        user_id_val,
        (ARRAY['COMMENT', 'LIKE', 'REPLY'])[floor(random() * 3 + 1)],
        jsonb_build_object(
            'categoryId', (ARRAY[text_cat_id, check_cat_id, checklist_cat_id, expense_cat_id, time_cat_id, number_cat_id])[floor(random() * 6 + 1)]::text,
            'message', 'Notification message #' || s.i
        ),
        random() > 0.5,
        NOW() - (s.i * interval '30 minutes')
    FROM generate_series(1, 200) AS s(i);

    -- Step 8: Text Record Images (text_record 중 200개에 이미지 1~2장씩)
    RAISE NOTICE 'Generating text record images...';
    INSERT INTO text_record_image (id, text_record_id, image_url, display_order)
    SELECT
        uuid_generate_v4(),
        tr.id,
        'https://picsum.photos/seed/' || tr.id || '-' || img.ord || '/800/600',
        img.ord
    FROM (
        SELECT id FROM text_record ORDER BY random() LIMIT 200
    ) tr
    CROSS JOIN LATERAL generate_series(1, (floor(random() * 2 + 1)::int)) AS img(ord);

    RAISE NOTICE 'Finished generating all dummy data.';
END $$;
