-- =============================================================================
-- Keep4Life 부하 테스트용 데이터 생성 SQL (MySQL 8.0)
-- =============================================================================
-- DBeaver, DataGrip 등 GUI 클라이언트에서도 실행 가능한 버전
-- 순서대로 블록별로 실행하세요
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0-1. 기존 테스트 데이터 삭제 (FK 제약조건 순서 고려)
-- -----------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM notification WHERE receiver_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
DELETE FROM chat_message WHERE chat_room_id IN (SELECT id FROM chat_room WHERE user1_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
DELETE FROM chat_room WHERE user1_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
DELETE FROM friendship WHERE requester_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%') OR receiver_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
DELETE FROM comment WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
DELETE FROM category_like WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
DELETE FROM expense_record_tag WHERE expense_record_id IN (SELECT id FROM expense_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%')));
DELETE FROM expense_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
DELETE FROM expense_tag WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
DELETE FROM check_list_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
DELETE FROM number_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
DELETE FROM time_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
DELETE FROM text_record_image WHERE text_record_id IN (SELECT id FROM text_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%')));
DELETE FROM text_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
DELETE FROM check_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
DELETE FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
DELETE FROM users WHERE email LIKE 'loadtest%';

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 0-2. 숫자 테이블 생성 (generate_series 대체용)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS numbers;
CREATE TABLE numbers (n INT PRIMARY KEY);

INSERT INTO numbers (n)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 500
)
SELECT n FROM seq;

-- ----------------------------ㄴ-------------------------------------------------
-- 1. 유저 500명 생성
-- -----------------------------------------------------------------------------
INSERT INTO users (id, email, username, nickname, password, authority)
SELECT
    UUID_TO_BIN(UUID()),
    CONCAT('loadtest', n, '@test.com'),
    CONCAT('loaduser', n),
    CONCAT('tester', n),
    'test1234',
    0  -- ROLE_USER (enum ordinal)
FROM numbers
WHERE n <= 500;

-- -----------------------------------------------------------------------------
-- 2. 카테고리 (유저당 5개, 타입 분배, 공개/비공개 혼합)
-- -----------------------------------------------------------------------------
INSERT INTO category (id, user_id, title, description, record_type, visibility, created_at)
SELECT
    UUID_TO_BIN(UUID()),
    u.id,
    CONCAT('카테고리 ', num.n),
    '부하 테스트용',
    ELT(num.n, 'CHECK', 'TEXT', 'TIME', 'NUMBER', 'EXPENSE'),
    IF(num.n <= 3, 'PUBLIC', 'PRIVATE'),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
FROM users u
CROSS JOIN (SELECT n FROM numbers WHERE n <= 5) num
WHERE u.email LIKE 'loadtest%';

-- -----------------------------------------------------------------------------
-- 3. check_record (CHECK 카테고리당 300일치) → 150,000건 목표
-- -----------------------------------------------------------------------------
INSERT INTO check_record (id, category_id, success, date)
SELECT
    UUID_TO_BIN(UUID()),
    c.id,
    RAND() > 0.3,
    DATE_SUB(CURDATE(), INTERVAL num.n - 1 DAY)
FROM category c
CROSS JOIN (SELECT n FROM numbers WHERE n <= 300) num
WHERE c.record_type = 'CHECK'
  AND c.user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');

-- -----------------------------------------------------------------------------
-- 4. text_record (TEXT 카테고리당 100건)
-- -----------------------------------------------------------------------------
INSERT INTO text_record (id, category_id, title, text, date)
SELECT
    UUID_TO_BIN(UUID()),
    c.id,
    CONCAT('텍스트 기록 ', num.n),
    CONCAT('부하 테스트 텍스트 내용 ', num.n),
    DATE_SUB(CURDATE(), INTERVAL FLOOR(num.n * 1.8) DAY)
FROM category c
CROSS JOIN (SELECT n FROM numbers WHERE n <= 100) num
WHERE c.record_type = 'TEXT'
  AND c.user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');

-- -----------------------------------------------------------------------------
-- 4-1. text_record_image (텍스트 기록당 이미지 2장) → 100,000건 목표
-- -----------------------------------------------------------------------------
INSERT INTO text_record_image (id, text_record_id, image_url, display_order)
SELECT
    UUID_TO_BIN(UUID()),
    tr.id,
    CONCAT('https://example.com/test-image-', num.n, '.jpg'),
    num.n
FROM text_record tr
CROSS JOIN (SELECT n FROM numbers WHERE n <= 2) num
WHERE tr.category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));

-- -----------------------------------------------------------------------------
-- 5. time_record (TIME 카테고리당 200일치) → 100,000건 목표
-- -----------------------------------------------------------------------------
INSERT INTO time_record (id, category_id, time, date)
SELECT
    UUID_TO_BIN(UUID()),
    c.id,
    SEC_TO_TIME(FLOOR(RAND() * 28800)),
    DATE_SUB(CURDATE(), INTERVAL num.n - 1 DAY)
FROM category c
CROSS JOIN (SELECT n FROM numbers WHERE n <= 200) num
WHERE c.record_type = 'TIME'
  AND c.user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');

-- -----------------------------------------------------------------------------
-- 6. number_record (NUMBER 카테고리당 200일치) → 100,000건 목표
-- -----------------------------------------------------------------------------
INSERT INTO number_record (id, category_id, number, date)
SELECT
    UUID_TO_BIN(UUID()),
    c.id,
    FLOOR(RAND() * 1000),
    DATE_SUB(CURDATE(), INTERVAL num.n - 1 DAY)
FROM category c
CROSS JOIN (SELECT n FROM numbers WHERE n <= 200) num
WHERE c.record_type = 'NUMBER'
  AND c.user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');

-- -----------------------------------------------------------------------------
-- 6-1. check_list_record (카테고리당 하루 3개 항목 * 50일) → 75,000건 목표
-- -----------------------------------------------------------------------------
INSERT INTO check_list_record (id, category_id, todo, success, date)
SELECT
    UUID_TO_BIN(UUID()),
    c.id,
    CONCAT('할 일 ', num.n),
    RAND() > 0.4,
    DATE_SUB(CURDATE(), INTERVAL FLOOR((num.n - 1) / 3) DAY)
FROM category c
CROSS JOIN (SELECT n FROM numbers WHERE n <= 150) num
WHERE c.record_type = 'CHECK'
  AND c.user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');

-- -----------------------------------------------------------------------------
-- 7. expense_tag (유저당 5개)
-- -----------------------------------------------------------------------------
INSERT INTO expense_tag (id, user_id, name)
SELECT
    UUID_TO_BIN(UUID()),
    u.id,
    ELT(num.n, '식비', '교통비', '생활비', '여가', '기타')
FROM users u
CROSS JOIN (SELECT n FROM numbers WHERE n <= 5) num
WHERE u.email LIKE 'loadtest%';

-- -----------------------------------------------------------------------------
-- 8. expense_record (EXPENSE 카테고리당 150건)
-- -----------------------------------------------------------------------------
INSERT INTO expense_record (id, category_id, amount, expense_type, payment_method, merchant, memo, date)
SELECT
    UUID_TO_BIN(UUID()),
    c.id,
    FLOOR(RAND() * 50000) + 1000,
    ELT(FLOOR(RAND() * 2) + 1, 'EXPENSE', 'INCOME'),
    ELT(FLOOR(RAND() * 3) + 1, 'CASH', 'CARD', 'TRANSFER'),
    CONCAT('가맹점 ', num.n),
    CONCAT('지출 항목 ', num.n),
    DATE_SUB(CURDATE(), INTERVAL FLOOR(num.n * 1.2) DAY)
FROM category c
CROSS JOIN (SELECT n FROM numbers WHERE n <= 150) num
WHERE c.record_type = 'EXPENSE'
  AND c.user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');

-- -----------------------------------------------------------------------------
-- 8-1. expense_record_tag (지출 기록당 태그 1~2개) → 100,000건 목표
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO expense_record_tag (id, expense_record_id, expense_tag_id)
SELECT
    UUID_TO_BIN(UUID()),
    er.id,
    (SELECT et.id FROM expense_tag et WHERE et.user_id = c.user_id ORDER BY RAND() LIMIT 1)
FROM expense_record er
JOIN category c ON er.category_id = c.id
CROSS JOIN (SELECT n FROM numbers WHERE n <= 2) num
WHERE c.user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');

-- -----------------------------------------------------------------------------
-- 9. category_like (공개 카테고리당 좋아요 10개)
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO category_like (id, user_id, category_id, created_at)
SELECT
    UUID_TO_BIN(UUID()),
    (SELECT id FROM users WHERE email LIKE 'loadtest%' ORDER BY RAND() LIMIT 1),
    c.id,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY)
FROM category c
CROSS JOIN (SELECT n FROM numbers WHERE n <= 10) num
WHERE c.visibility = 'PUBLIC';

-- -----------------------------------------------------------------------------
-- 10. comment (공개 카테고리당 댓글 5개)
-- -----------------------------------------------------------------------------
INSERT INTO comment (id, comment, parent_id, category_id, user_id, created_at)
SELECT
    UUID_TO_BIN(UUID()),
    CONCAT('테스트 댓글 ', num.n),
    NULL,
    c.id,
    (SELECT id FROM users WHERE email LIKE 'loadtest%' ORDER BY RAND() LIMIT 1),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 1000) HOUR)
FROM category c
CROSS JOIN (SELECT n FROM numbers WHERE n <= 5) num
WHERE c.visibility = 'PUBLIC';

-- -----------------------------------------------------------------------------
-- 11. friendship (유저당 친구 10명) - 일부만 생성
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO friendship (id, requester_id, receiver_id, status, created_at)
SELECT
    UUID_TO_BIN(UUID()),
    u1.id,
    u2.id,
    'ACCEPTED',
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY)
FROM users u1
CROSS JOIN users u2
WHERE u1.email LIKE 'loadtest%'
  AND u2.email LIKE 'loadtest%'
  AND u1.id < u2.id
  AND RAND() < 0.02  -- 약 2%만 친구 관계 생성
LIMIT 5000;

-- -----------------------------------------------------------------------------
-- 12. chat_room (친구 쌍당 채팅방)
-- -----------------------------------------------------------------------------
INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
SELECT
    UUID_TO_BIN(UUID()),
    LEAST(f.requester_id, f.receiver_id),
    GREATEST(f.requester_id, f.receiver_id),
    f.created_at,
    NOW()
FROM friendship f
WHERE f.status = 'ACCEPTED'
  AND f.requester_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%')
  AND f.receiver_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%')
LIMIT 2500;

-- -----------------------------------------------------------------------------
-- 13. chat_message (채팅방당 50건)
-- -----------------------------------------------------------------------------
INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
SELECT
    UUID_TO_BIN(UUID()),
    cr.id,
    IF(RAND() > 0.5, cr.user1_id, cr.user2_id),
    CONCAT('테스트 메시지 ', num.n),
    RAND() > 0.3,
    DATE_ADD(cr.created_at, INTERVAL num.n MINUTE)
FROM chat_room cr
CROSS JOIN (SELECT n FROM numbers WHERE n <= 50) num
WHERE cr.user1_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%')
  AND cr.user2_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');

-- -----------------------------------------------------------------------------
-- 14. notification (유저당 100건)
-- -----------------------------------------------------------------------------
INSERT INTO notification (id, receiver_id, sender_id, type, data, `read`, created_at)
SELECT
    UUID_TO_BIN(UUID()),
    u.id,
    (SELECT id FROM users WHERE id != u.id AND email LIKE 'loadtest%' ORDER BY RAND() LIMIT 1),
    ELT(FLOOR(RAND() * 5) + 1, 'COMMENT', 'LIKE', 'REPLY', 'FRIEND_REQUEST', 'FRIEND_ACCEPT'),
    '{"categoryTitle":"테스트"}',
    RAND() > 0.5,
    DATE_SUB(NOW(), INTERVAL num.n MINUTE)
FROM users u
CROSS JOIN (SELECT n FROM numbers WHERE n <= 100) num
WHERE u.email LIKE 'loadtest%';

-- -----------------------------------------------------------------------------
-- 15. 정리 및 통계 갱신
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS numbers;

ANALYZE TABLE users, category, check_record, text_record, time_record,
    number_record, expense_record, expense_tag,
    category_like, comment, friendship, chat_room, chat_message, notification;

-- =============================================================================
-- 데이터 정리용 SQL (테스트 후 주석 해제하여 실행)
-- =============================================================================
-- DELETE FROM notification WHERE receiver_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
-- DELETE FROM chat_message WHERE chat_room_id IN (SELECT id FROM chat_room WHERE user1_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
-- DELETE FROM chat_room WHERE user1_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
-- DELETE FROM friendship WHERE requester_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
-- DELETE FROM comment WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
-- DELETE FROM category_like WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
-- DELETE FROM expense_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
-- DELETE FROM expense_tag WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
-- DELETE FROM number_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
-- DELETE FROM time_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
-- DELETE FROM text_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
-- DELETE FROM check_record WHERE category_id IN (SELECT id FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%'));
-- DELETE FROM category WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest%');
-- DELETE FROM users WHERE email LIKE 'loadtest%';
