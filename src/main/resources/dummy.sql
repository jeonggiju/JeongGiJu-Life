-- ===================================
-- Health Tracking Dummy Data (1000 rows each)
-- Generate data for approximately 1000 days (2022-04-25 ~ 2025-01-19)
-- ===================================

-- Smoking Data (1000 rows)
-- Pattern: 약 15% 정도 흡연 (랜덤)
INSERT INTO smoking (smoke, date)
SELECT
    (RANDOM() < 0.15) AS smoke,  -- 15% 확률로 흡연
    (CURRENT_DATE - generate_series)::DATE AS date
FROM generate_series(0, 999);

-- Sleep Data (1000 rows)
-- Pattern: 취침 21:00~24:00, 기상 05:00~08:00, view_phone 약 20% 확률
INSERT INTO sleep (sleep_time, wake_time, view_phone, date)
SELECT
    -- 취침시간: 21:00 ~ 24:00 사이 랜덤
    ('21:00:00'::TIME + (RANDOM() * INTERVAL '3 hours')) AS sleep_time,
    -- 기상시간: 05:00 ~ 08:00 사이 랜덤
    ('05:00:00'::TIME + (RANDOM() * INTERVAL '3 hours')) AS wake_time,
    -- 20% 확률로 폰 봄
    (RANDOM() < 0.2) AS view_phone,
    (CURRENT_DATE - generate_series)::DATE AS date
FROM generate_series(0, 999);

-- Sex Data (1000 rows)
-- Pattern: 약 10% 확률
INSERT INTO sex (sex, date)
SELECT
    (RANDOM() < 0.1) AS sex,  -- 10% 확률
    (CURRENT_DATE - generate_series)::DATE AS date
FROM generate_series(0, 999);

-- Exercise Data (1000 rows)
-- Pattern: 약 70% 운동, 운동시간 07:00~09:00
INSERT INTO exercise (work, time, date)
SELECT
    (RANDOM() < 0.7) AS work,  -- 70% 확률로 운동
    CASE
        WHEN (RANDOM() < 0.7) THEN ('07:00:00'::TIME + (RANDOM() * INTERVAL '2 hours'))
        ELSE NULL
        END AS time,
    (CURRENT_DATE - generate_series)::DATE AS date
FROM generate_series(0, 999);

-- Diet Data (1000 rows)
-- Pattern: 약 10% (주말에 더 높은 확률)
INSERT INTO diet (drink, date)
SELECT
    CASE
        -- 금요일(5), 토요일(6) = 25% 확률
        WHEN EXTRACT(DOW FROM date_val) IN (5, 6) THEN (RANDOM() < 0.25)
        -- 평일 = 5% 확률
        ELSE (RANDOM() < 0.05)
        END AS drink,
    date_val AS date
FROM (
         SELECT (CURRENT_DATE - generate_series)::DATE AS date_val
         FROM generate_series(0, 999)
     ) sub;

-- Caffeine Data (1000 rows)
-- Pattern: 약 80% 카페인 섭취
INSERT INTO caffeine (drink, date)
SELECT
    (RANDOM() < 0.8) AS drink,  -- 80% 확률로 카페인 섭취
    (CURRENT_DATE - generate_series)::DATE AS date
FROM generate_series(0, 999);

-- Alcohol Data (1000 rows)
-- Pattern: 약 15% 음주 (주말에 더 높은 확률)
INSERT INTO alcohol (drink, date)
SELECT
    CASE
        -- 금요일(5), 토요일(6) = 35% 확률
        WHEN EXTRACT(DOW FROM date_val) IN (5, 6) THEN (RANDOM() < 0.35)
        -- 평일 = 8% 확률
        ELSE (RANDOM() < 0.08)
        END AS drink,
    date_val AS date
FROM (
         SELECT (CURRENT_DATE - generate_series)::DATE AS date_val
         FROM generate_series(0, 999)
     ) sub;

-- ===================================
-- 데이터 확인 쿼리
-- ===================================
SELECT 'smoking' as table_name, COUNT(*) as total_count,
       SUM(CASE WHEN smoke = true THEN 1 ELSE 0 END) as true_count,
       ROUND(100.0 * SUM(CASE WHEN smoke = true THEN 1 ELSE 0 END) / COUNT(*), 2) as true_percentage
FROM smoking
UNION ALL
SELECT 'sleep', COUNT(*),
       SUM(CASE WHEN view_phone = true THEN 1 ELSE 0 END),
       ROUND(100.0 * SUM(CASE WHEN view_phone = true THEN 1 ELSE 0 END) / COUNT(*), 2)
FROM sleep
UNION ALL
SELECT 'sex', COUNT(*),
       SUM(CASE WHEN sex = true THEN 1 ELSE 0 END),
       ROUND(100.0 * SUM(CASE WHEN sex = true THEN 1 ELSE 0 END) / COUNT(*), 2)
FROM sex
UNION ALL
SELECT 'exercise', COUNT(*),
       SUM(CASE WHEN work = true THEN 1 ELSE 0 END),
       ROUND(100.0 * SUM(CASE WHEN work = true THEN 1 ELSE 0 END) / COUNT(*), 2)
FROM exercise
UNION ALL
SELECT 'diet', COUNT(*),
       SUM(CASE WHEN drink = true THEN 1 ELSE 0 END),
       ROUND(100.0 * SUM(CASE WHEN drink = true THEN 1 ELSE 0 END) / COUNT(*), 2)
FROM diet
UNION ALL
SELECT 'caffeine', COUNT(*),
       SUM(CASE WHEN drink = true THEN 1 ELSE 0 END),
       ROUND(100.0 * SUM(CASE WHEN drink = true THEN 1 ELSE 0 END) / COUNT(*), 2)
FROM caffeine
UNION ALL
SELECT 'alcohol', COUNT(*),
       SUM(CASE WHEN drink = true THEN 1 ELSE 0 END),
       ROUND(100.0 * SUM(CASE WHEN drink = true THEN 1 ELSE 0 END) / COUNT(*), 2)
FROM alcohol
ORDER BY table_name;

-- 날짜 범위 확인
SELECT
    'Date Range' as info,
    MIN(date) as earliest_date,
    MAX(date) as latest_date,
    MAX(date) - MIN(date) + 1 as total_days
FROM (
         SELECT date FROM smoking
         UNION SELECT date FROM sleep
         UNION SELECT date FROM sex
         UNION SELECT date FROM exercise
         UNION SELECT date FROM diet
         UNION SELECT date FROM caffeine
         UNION SELECT date FROM alcohol
     ) all_dates;

-- 최근 10일 데이터 샘플 확인
SELECT
    s.date,
    s.smoke as smoking,
    sl.sleep_time,
    sl.wake_time,
    sl.view_phone,
    sx.sex as sex,
    e.work as exercise,
    e.time as exercise_time,
    d.drink as diet,
    c.drink as caffeine,
    a.drink as alcohol
FROM smoking s
         LEFT JOIN sleep sl ON s.date = sl.date
         LEFT JOIN sex sx ON s.date = sx.date
         LEFT JOIN exercise e ON s.date = e.date
         LEFT JOIN diet d ON s.date = d.date
         LEFT JOIN caffeine c ON s.date = c.date
         LEFT JOIN alcohol a ON s.date = a.date
ORDER BY s.date DESC
LIMIT 10;