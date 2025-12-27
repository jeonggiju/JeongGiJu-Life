CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

TRUNCATE TABLE
    check_record,
    text_record,
    time_record,
    category,
    users
    CASCADE;

INSERT INTO users (id, username, password, email, authority, title, description)
VALUES
    (uuid_generate_v4(), 'jeonggiju', '$2a$10$dummyhash1', 'jeonggiju@test.com', 1, '정기주', '메인 사용자'),
    (uuid_generate_v4(), 'admin', '$2a$10$dummyhash2', 'admin@test.com', 0, '관리자', '시스템 관리자');

SELECT id, username FROM users;

INSERT INTO category (id, record_type, user_id, title, description)
VALUES
    (uuid_generate_v4(), 0, 'u1', '금연', '하루 금연 체크'),
    (uuid_generate_v4(), 1, 'u1', '일기', '텍스트 기록용'),
    (uuid_generate_v4(), 2, 'u1', '수면', '수면 시간 기록'),
    (uuid_generate_v4(), 0, 'u2', '관리자 체크', '관리자 전용 체크');

SELECT id, title FROM category;

INSERT INTO check_record (id, created_at, success, category_id)
VALUES
    (uuid_generate_v4(), NOW() - INTERVAL '2 days', true, 'c1'),
    (uuid_generate_v4(), NOW() - INTERVAL '1 days', false, 'c1'),
    (uuid_generate_v4(), NOW(), true, 'c4');

INSERT INTO text_record (id, created_at, title, text, category_id)
VALUES
    (uuid_generate_v4(), NOW() - INTERVAL '1 days', '오늘의 일기', '오늘은 꽤 생산적인 하루였다.', 'c2'),
    (uuid_generate_v4(), NOW(), '회고', '커피를 안 마시니 집중력이 떨어진다.', 'c2');

INSERT INTO time_record (id, created_at, date, category_id)
VALUES
    (uuid_generate_v4(), NOW() - INTERVAL '1 days', '2025-12-26', 'c3'),
    (uuid_generate_v4(), NOW(), '2025-12-27', 'c3');

SELECT * FROM users;
SELECT * FROM category;
SELECT * FROM check_record;
SELECT * FROM text_record;
SELECT * FROM time_record;
