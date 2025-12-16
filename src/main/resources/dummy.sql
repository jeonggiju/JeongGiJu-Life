-- Caffeine 테이블 더미 데이터 1000개
INSERT INTO caffeine (drink, date)
SELECT
    CASE WHEN RANDOM() > 0.3 THEN true ELSE false END as drink,
    CURRENT_DATE - generate_series as date
FROM generate_series(0, 999);

-- Diet 테이블 더미 데이터 1000개
INSERT INTO diet (drink, date)
SELECT
    CASE WHEN RANDOM() > 0.2 THEN true ELSE false END as drink,
    CURRENT_DATE - generate_series as date
FROM generate_series(0, 999);

-- Sleep 테이블 더미 데이터 1000개
INSERT INTO sleep (sleep_time, wake_time, date)
SELECT
    (TIME '22:00:00' + (RANDOM() * INTERVAL '4 hours'))::time as sleep_time,
    (TIME '06:00:00' + (RANDOM() * INTERVAL '4 hours'))::time as wake_time,
    CURRENT_DATE - generate_series as date
FROM generate_series(0, 999);

-- Smoking 테이블 더미 데이터 1000개
INSERT INTO smoking (smoke, date)
SELECT
    CASE WHEN RANDOM() > 0.9 THEN true ELSE false END as smoke,
    CURRENT_DATE - generate_series as date
FROM generate_series(0, 999);