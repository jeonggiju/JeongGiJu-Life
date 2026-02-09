-- =========================================================
-- Keep4Life 통합 더미 데이터
-- MySQL 8.0
-- 전제: ddl-auto: update로 테이블이 자동 생성된 후 실행
-- 실행: mysql -u root -p app < dummy_data.sql
-- =========================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS generate_dummy_data$$

CREATE PROCEDURE generate_dummy_data()
BEGIN

    -- -------------------------------------------------------
    -- 변수 선언
    -- -------------------------------------------------------
    DECLARE i INT;
    DECLARE j INT;
    DECLARE k INT;
    DECLARE v_user_id CHAR(36);
    DECLARE v_cat_id CHAR(36);
    DECLARE v_parent_id CHAR(36);
    DECLARE v_room_id CHAR(36);
    DECLARE v_user1_id CHAR(36);
    DECLARE v_user2_id CHAR(36);
    DECLARE v_record_type VARCHAR(20);
    DECLARE v_cnt INT;
    DECLARE v_ts DATETIME(6);
    DECLARE v_rand DOUBLE;

    -- -------------------------------------------------------
    -- 전체 초기화
    -- -------------------------------------------------------
    SET FOREIGN_KEY_CHECKS = 0;
    TRUNCATE TABLE chat_message;
    TRUNCATE TABLE chat_room;
    TRUNCATE TABLE friendship;
    TRUNCATE TABLE notification;
    TRUNCATE TABLE category_like;
    TRUNCATE TABLE `comment`;
    TRUNCATE TABLE expense_record_tag;
    TRUNCATE TABLE expense_record;
    TRUNCATE TABLE expense_tag;
    TRUNCATE TABLE check_list_record;
    TRUNCATE TABLE check_record;
    TRUNCATE TABLE number_record;
    TRUNCATE TABLE text_record_image;
    TRUNCATE TABLE text_record;
    TRUNCATE TABLE time_record;
    TRUNCATE TABLE category;
    TRUNCATE TABLE users;
    SET FOREIGN_KEY_CHECKS = 1;

    -- 임시 테이블 생성
    DROP TEMPORARY TABLE IF EXISTS tmp_users;
    DROP TEMPORARY TABLE IF EXISTS tmp_categories;
    CREATE TEMPORARY TABLE tmp_users (id CHAR(36) PRIMARY KEY, rn INT);
    CREATE TEMPORARY TABLE tmp_categories (id CHAR(36) PRIMARY KEY, record_type VARCHAR(20) NOT NULL);

    -- =========================================================
    -- 1) users 10명
    -- =========================================================
    SET i = 1;
    WHILE i <= 10 DO
        SET v_user_id = UUID();
        INSERT INTO users (id, authority, birth_day, birth_month, birth_year, description, email, password, title, username, nickname, profile_image_url)
        VALUES (
            v_user_id, 1,
            FLOOR(RAND() * 28) + 1,
            FLOOR(RAND() * 12) + 1,
            FLOOR(RAND() * 10) + 1995,
            CONCAT('더미 사용자 ', i),
            CONCAT('user', LPAD(i, 2, '0'), '@example.com'),
            'pass1234', 'USER',
            CONCAT('user', LPAD(i, 2, '0')),
            CONCAT('nickname', LPAD(i, 2, '0')),
            NULL
        );
        INSERT INTO tmp_users (id, rn) VALUES (v_user_id, i);
        SET i = i + 1;
    END WHILE;

    -- =========================================================
    -- 2) category 600개 (record_type 6종 균등)
    -- =========================================================
    SET i = 1;
    WHILE i <= 600 DO
        SET v_cat_id = UUID();
        SET v_record_type = ELT((i % 6) + 1, 'CHECK', 'CHECKLIST', 'TEXT', 'TIME', 'NUMBER', 'EXPENSE');

        SELECT id INTO v_user_id FROM tmp_users WHERE rn = ((i - 1) % 10) + 1;

        SET v_ts = NOW() - INTERVAL FLOOR(RAND() * 90) DAY;

        INSERT INTO category (id, user_id, description, record_type, title, visibility, created_at)
        VALUES (
            v_cat_id, v_user_id,
            CONCAT('더미 카테고리 설명 ', i),
            v_record_type,
            CONCAT(
                ELT((i % 6) + 1, '습관체크-', '할일-', '일기-', '시간기록-', '숫자기록-', '가계부-'),
                i
            ),
            IF(RAND() < 0.6, 'PUBLIC', 'PRIVATE'),
            v_ts
        );
        INSERT INTO tmp_categories (id, record_type) VALUES (v_cat_id, v_record_type);
        SET i = i + 1;
    END WHILE;

    -- =========================================================
    -- 3) category_like 8000개 (INSERT IGNORE for uniqueness)
    -- =========================================================
    BEGIN
        DECLARE v_like_cat CHAR(36);
        DECLARE v_like_user CHAR(36);
        DECLARE v_cat_cnt INT;
        DECLARE v_user_cnt INT;

        SELECT COUNT(*) INTO v_cat_cnt FROM tmp_categories;
        SELECT COUNT(*) INTO v_user_cnt FROM tmp_users;

        SET i = 1;
        WHILE i <= 8000 DO
            SELECT id INTO v_like_cat FROM tmp_categories LIMIT 1 OFFSET FLOOR(RAND() * v_cat_cnt);
            SELECT id INTO v_like_user FROM tmp_users LIMIT 1 OFFSET FLOOR(RAND() * v_user_cnt);

            INSERT IGNORE INTO category_like (id, created_at, category_id, user_id)
            VALUES (UUID(), NOW() - INTERVAL FLOOR(RAND() * 90) DAY, v_like_cat, v_like_user);

            SET i = i + 1;
        END WHILE;
    END;

    -- =========================================================
    -- 4) comment 더미데이터 (최대 5단)
    -- =========================================================
    DROP TEMPORARY TABLE IF EXISTS tmp_level1;
    DROP TEMPORARY TABLE IF EXISTS tmp_level2;
    DROP TEMPORARY TABLE IF EXISTS tmp_level3;
    DROP TEMPORARY TABLE IF EXISTS tmp_level4;
    DROP TEMPORARY TABLE IF EXISTS tmp_level5;
    CREATE TEMPORARY TABLE tmp_level1 (id CHAR(36) PRIMARY KEY, category_id CHAR(36) NOT NULL);
    CREATE TEMPORARY TABLE tmp_level2 (id CHAR(36) PRIMARY KEY, category_id CHAR(36) NOT NULL);
    CREATE TEMPORARY TABLE tmp_level3 (id CHAR(36) PRIMARY KEY, category_id CHAR(36) NOT NULL);
    CREATE TEMPORARY TABLE tmp_level4 (id CHAR(36) PRIMARY KEY, category_id CHAR(36) NOT NULL);
    CREATE TEMPORARY TABLE tmp_level5 (id CHAR(36) PRIMARY KEY, category_id CHAR(36) NOT NULL);

    -- depth=1 루트 6000
    BEGIN
        DECLARE v_cmt_id CHAR(36);
        DECLARE v_cmt_cat CHAR(36);
        DECLARE v_cmt_user CHAR(36);
        DECLARE v_cat_cnt INT;
        DECLARE v_user_cnt INT;

        SELECT COUNT(*) INTO v_cat_cnt FROM tmp_categories;
        SELECT COUNT(*) INTO v_user_cnt FROM tmp_users;

        SET i = 1;
        WHILE i <= 6000 DO
            SET v_cmt_id = UUID();
            SELECT id INTO v_cmt_cat FROM tmp_categories LIMIT 1 OFFSET FLOOR(RAND() * v_cat_cnt);
            SELECT id INTO v_cmt_user FROM tmp_users LIMIT 1 OFFSET FLOOR(RAND() * v_user_cnt);
            SET v_ts = NOW() - INTERVAL FLOOR(RAND() * 60 * 24 * 60) MINUTE;

            INSERT INTO `comment` (id, comment, parent_id, category_id, user_id, created_at, updated_at)
            VALUES (v_cmt_id, CONCAT('댓글(d1) - ', i), NULL, v_cmt_cat, v_cmt_user, v_ts, v_ts);

            INSERT INTO tmp_level1 (id, category_id) VALUES (v_cmt_id, v_cmt_cat);
            SET i = i + 1;
        END WHILE;
    END;

    -- depth=2 (70% / 1~4 replies)
    BEGIN
        DECLARE v_pid CHAR(36);
        DECLARE v_pcid CHAR(36);
        DECLARE v_cmt_id CHAR(36);
        DECLARE v_cmt_user CHAR(36);
        DECLARE v_user_cnt INT;
        DECLARE v_reply_cnt INT;
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id, category_id FROM tmp_level1;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        SELECT COUNT(*) INTO v_user_cnt FROM tmp_users;
        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_pid, v_pcid;
            IF done THEN LEAVE read_loop; END IF;
            IF RAND() < 0.70 THEN
                SET v_reply_cnt = FLOOR(RAND() * 4) + 1;
                SET j = 1;
                WHILE j <= v_reply_cnt DO
                    SET v_cmt_id = UUID();
                    SELECT id INTO v_cmt_user FROM tmp_users LIMIT 1 OFFSET FLOOR(RAND() * v_user_cnt);
                    SET v_ts = NOW() - INTERVAL FLOOR(RAND() * 60 * 24 * 60) MINUTE;
                    INSERT INTO `comment` (id, comment, parent_id, category_id, user_id, created_at, updated_at)
                    VALUES (v_cmt_id, CONCAT('댓글(d2) - ', v_pid, '-', j), v_pid, v_pcid, v_cmt_user, v_ts, v_ts);
                    INSERT INTO tmp_level2 (id, category_id) VALUES (v_cmt_id, v_pcid);
                    SET j = j + 1;
                END WHILE;
            END IF;
        END LOOP;
        CLOSE cur;
    END;

    -- depth=3 (50% / 1~3)
    BEGIN
        DECLARE v_pid CHAR(36);
        DECLARE v_pcid CHAR(36);
        DECLARE v_cmt_id CHAR(36);
        DECLARE v_cmt_user CHAR(36);
        DECLARE v_user_cnt INT;
        DECLARE v_reply_cnt INT;
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id, category_id FROM tmp_level2;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        SELECT COUNT(*) INTO v_user_cnt FROM tmp_users;
        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_pid, v_pcid;
            IF done THEN LEAVE read_loop; END IF;
            IF RAND() < 0.50 THEN
                SET v_reply_cnt = FLOOR(RAND() * 3) + 1;
                SET j = 1;
                WHILE j <= v_reply_cnt DO
                    SET v_cmt_id = UUID();
                    SELECT id INTO v_cmt_user FROM tmp_users LIMIT 1 OFFSET FLOOR(RAND() * v_user_cnt);
                    SET v_ts = NOW() - INTERVAL FLOOR(RAND() * 60 * 24 * 60) MINUTE;
                    INSERT INTO `comment` (id, comment, parent_id, category_id, user_id, created_at, updated_at)
                    VALUES (v_cmt_id, CONCAT('댓글(d3) - ', v_pid, '-', j), v_pid, v_pcid, v_cmt_user, v_ts, v_ts);
                    INSERT INTO tmp_level3 (id, category_id) VALUES (v_cmt_id, v_pcid);
                    SET j = j + 1;
                END WHILE;
            END IF;
        END LOOP;
        CLOSE cur;
    END;

    -- depth=4 (35% / 1~2)
    BEGIN
        DECLARE v_pid CHAR(36);
        DECLARE v_pcid CHAR(36);
        DECLARE v_cmt_id CHAR(36);
        DECLARE v_cmt_user CHAR(36);
        DECLARE v_user_cnt INT;
        DECLARE v_reply_cnt INT;
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id, category_id FROM tmp_level3;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        SELECT COUNT(*) INTO v_user_cnt FROM tmp_users;
        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_pid, v_pcid;
            IF done THEN LEAVE read_loop; END IF;
            IF RAND() < 0.35 THEN
                SET v_reply_cnt = FLOOR(RAND() * 2) + 1;
                SET j = 1;
                WHILE j <= v_reply_cnt DO
                    SET v_cmt_id = UUID();
                    SELECT id INTO v_cmt_user FROM tmp_users LIMIT 1 OFFSET FLOOR(RAND() * v_user_cnt);
                    SET v_ts = NOW() - INTERVAL FLOOR(RAND() * 60 * 24 * 60) MINUTE;
                    INSERT INTO `comment` (id, comment, parent_id, category_id, user_id, created_at, updated_at)
                    VALUES (v_cmt_id, CONCAT('댓글(d4) - ', v_pid, '-', j), v_pid, v_pcid, v_cmt_user, v_ts, v_ts);
                    INSERT INTO tmp_level4 (id, category_id) VALUES (v_cmt_id, v_pcid);
                    SET j = j + 1;
                END WHILE;
            END IF;
        END LOOP;
        CLOSE cur;
    END;

    -- depth=5 (20% / 1)
    BEGIN
        DECLARE v_pid CHAR(36);
        DECLARE v_pcid CHAR(36);
        DECLARE v_cmt_id CHAR(36);
        DECLARE v_cmt_user CHAR(36);
        DECLARE v_user_cnt INT;
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id, category_id FROM tmp_level4;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        SELECT COUNT(*) INTO v_user_cnt FROM tmp_users;
        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_pid, v_pcid;
            IF done THEN LEAVE read_loop; END IF;
            IF RAND() < 0.20 THEN
                SET v_cmt_id = UUID();
                SELECT id INTO v_cmt_user FROM tmp_users LIMIT 1 OFFSET FLOOR(RAND() * v_user_cnt);
                SET v_ts = NOW() - INTERVAL FLOOR(RAND() * 60 * 24 * 60) MINUTE;
                INSERT INTO `comment` (id, comment, parent_id, category_id, user_id, created_at, updated_at)
                VALUES (v_cmt_id, CONCAT('댓글(d5) - ', v_pid, '-1'), v_pid, v_pcid, v_cmt_user, v_ts, v_ts);
                INSERT INTO tmp_level5 (id, category_id) VALUES (v_cmt_id, v_pcid);
            END IF;
        END LOOP;
        CLOSE cur;
    END;

    -- =========================================================
    -- 5) check_record: CHECK 카테고리당 최근 120일
    -- =========================================================
    BEGIN
        DECLARE v_cr_cat CHAR(36);
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id FROM tmp_categories WHERE record_type = 'CHECK';
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_cr_cat;
            IF done THEN LEAVE read_loop; END IF;
            SET i = 0;
            WHILE i <= 119 DO
                INSERT INTO check_record (id, date, success, category_id)
                VALUES (UUID(), CURDATE() - INTERVAL i DAY, IF(RAND() < 0.75, 1, 0), v_cr_cat);
                SET i = i + 1;
            END WHILE;
        END LOOP;
        CLOSE cur;
    END;

    -- =========================================================
    -- 6) check_list_record: CHECKLIST 카테고리당 최근 60일 × 4개
    -- =========================================================
    BEGIN
        DECLARE v_cl_cat CHAR(36);
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id FROM tmp_categories WHERE record_type = 'CHECKLIST';
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_cl_cat;
            IF done THEN LEAVE read_loop; END IF;
            SET i = 0;
            WHILE i <= 59 DO
                SET j = 1;
                WHILE j <= 4 DO
                    INSERT INTO check_list_record (id, date, success, category_id, todo)
                    VALUES (UUID(), CURDATE() - INTERVAL i DAY, IF(RAND() < 0.6, 1, 0), v_cl_cat, CONCAT('TODO-', i, '-', j));
                    SET j = j + 1;
                END WHILE;
                SET i = i + 1;
            END WHILE;
        END LOOP;
        CLOSE cur;
    END;

    -- =========================================================
    -- 7) text_record: TEXT 카테고리당 최근 90일
    -- =========================================================
    BEGIN
        DECLARE v_tr_cat CHAR(36);
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id FROM tmp_categories WHERE record_type = 'TEXT';
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_tr_cat;
            IF done THEN LEAVE read_loop; END IF;
            SET i = 0;
            WHILE i <= 89 DO
                INSERT INTO text_record (id, date, category_id, text, title)
                VALUES (UUID(), CURDATE() - INTERVAL i DAY, v_tr_cat, CONCAT('더미 텍스트 내용 - day ', i), CONCAT('제목-', i));
                SET i = i + 1;
            END WHILE;
        END LOOP;
        CLOSE cur;
    END;

    -- =========================================================
    -- 8) time_record: TIME 카테고리당 최근 90일 (0~6시간 랜덤)
    -- =========================================================
    BEGIN
        DECLARE v_tmr_cat CHAR(36);
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id FROM tmp_categories WHERE record_type = 'TIME';
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_tmr_cat;
            IF done THEN LEAVE read_loop; END IF;
            SET i = 0;
            WHILE i <= 89 DO
                INSERT INTO time_record (id, date, time, category_id)
                VALUES (UUID(), CURDATE() - INTERVAL i DAY,
                        SEC_TO_TIME(FLOOR(RAND() * 6 * 3600)),
                        v_tmr_cat);
                SET i = i + 1;
            END WHILE;
        END LOOP;
        CLOSE cur;
    END;

    -- =========================================================
    -- 9) number_record: NUMBER 카테고리당 최근 120일 (0~100)
    -- =========================================================
    BEGIN
        DECLARE v_nr_cat CHAR(36);
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id FROM tmp_categories WHERE record_type = 'NUMBER';
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_nr_cat;
            IF done THEN LEAVE read_loop; END IF;
            SET i = 0;
            WHILE i <= 119 DO
                INSERT INTO number_record (id, date, number, category_id)
                VALUES (UUID(), CURDATE() - INTERVAL i DAY, FLOOR(RAND() * 101), v_nr_cat);
                SET i = i + 1;
            END WHILE;
        END LOOP;
        CLOSE cur;
    END;

    -- =========================================================
    -- 10) expense_tag: 유저별 태그 생성
    -- =========================================================
    BEGIN
        DECLARE v_et_user CHAR(36);
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id FROM tmp_users;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_et_user;
            IF done THEN LEAVE read_loop; END IF;
            INSERT INTO expense_tag (id, name, user_id) VALUES (UUID(), '식비', v_et_user);
            INSERT INTO expense_tag (id, name, user_id) VALUES (UUID(), '교통', v_et_user);
            INSERT INTO expense_tag (id, name, user_id) VALUES (UUID(), '쇼핑', v_et_user);
            INSERT INTO expense_tag (id, name, user_id) VALUES (UUID(), '의료', v_et_user);
            INSERT INTO expense_tag (id, name, user_id) VALUES (UUID(), '여가', v_et_user);
            INSERT INTO expense_tag (id, name, user_id) VALUES (UUID(), '구독', v_et_user);
            INSERT INTO expense_tag (id, name, user_id) VALUES (UUID(), '기타', v_et_user);
        END LOOP;
        CLOSE cur;
    END;

    -- =========================================================
    -- 11) expense_record: EXPENSE 카테고리당 최근 90일 (하루 1~3건)
    -- =========================================================
    BEGIN
        DECLARE v_er_cat CHAR(36);
        DECLARE v_expense_types VARCHAR(255);
        DECLARE v_payment_methods VARCHAR(255);
        DECLARE v_merchants VARCHAR(255);
        DECLARE v_t_cnt INT;
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR SELECT id FROM tmp_categories WHERE record_type = 'EXPENSE';
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO v_er_cat;
            IF done THEN LEAVE read_loop; END IF;
            SET i = 0;
            WHILE i <= 89 DO
                SET v_t_cnt = FLOOR(RAND() * 3) + 1;
                SET j = 1;
                WHILE j <= v_t_cnt DO
                    INSERT INTO expense_record (id, amount, expense_type, payment_method, merchant, memo, date, category_id)
                    VALUES (
                        UUID(),
                        ROUND(RAND() * 50000 + 1000, 0),
                        ELT(FLOOR(RAND() * 4) + 1, 'INCOME', 'EXPENSE', 'EXPENSE', 'EXPENSE'),
                        ELT(FLOOR(RAND() * 4) + 1, 'CASH', 'CARD', 'TRANSFER', 'OTHER'),
                        ELT(FLOOR(RAND() * 9) + 1, '편의점', '카페', '마트', '식당', '주유소', '병원', '온라인쇼핑', '교통', '구독'),
                        CONCAT('메모 ', i, '-', j),
                        CURDATE() - INTERVAL i DAY,
                        v_er_cat
                    );
                    SET j = j + 1;
                END WHILE;
                SET i = i + 1;
            END WHILE;
        END LOOP;
        CLOSE cur;
    END;

    -- =========================================================
    -- 12) friendship 데이터
    -- =========================================================
    -- ACCEPTED: user1 ↔ user2~5
    SET i = 2;
    WHILE i <= 5 DO
        SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 1;
        SELECT id INTO v_user2_id FROM tmp_users WHERE rn = i;
        INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
        VALUES (UUID(), v_user1_id, v_user2_id, 'ACCEPTED',
                NOW() - INTERVAL 30 DAY + INTERVAL i DAY,
                NOW() - INTERVAL 29 DAY + INTERVAL i DAY);
        SET i = i + 1;
    END WHILE;

    -- ACCEPTED: user2 ↔ user3
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 2;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 3;
    INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
    VALUES (UUID(), v_user1_id, v_user2_id, 'ACCEPTED', NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 19 DAY);

    -- ACCEPTED: user2 ↔ user4
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 2;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 4;
    INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
    VALUES (UUID(), v_user1_id, v_user2_id, 'ACCEPTED', NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 17 DAY);

    -- ACCEPTED: user3 ↔ user6
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 3;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 6;
    INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
    VALUES (UUID(), v_user1_id, v_user2_id, 'ACCEPTED', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 14 DAY);

    -- ACCEPTED: user4 ↔ user7
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 4;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 7;
    INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
    VALUES (UUID(), v_user1_id, v_user2_id, 'ACCEPTED', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY);

    -- PENDING: user6 → user1
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 6;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 1;
    INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
    VALUES (UUID(), v_user1_id, v_user2_id, 'PENDING', NOW() - INTERVAL 2 DAY, NULL);

    -- PENDING: user7 → user2
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 7;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 2;
    INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
    VALUES (UUID(), v_user1_id, v_user2_id, 'PENDING', NOW() - INTERVAL 1 DAY, NULL);

    -- PENDING: user8 → user3
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 8;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 3;
    INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
    VALUES (UUID(), v_user1_id, v_user2_id, 'PENDING', NOW() - INTERVAL 3 HOUR, NULL);

    -- REJECTED: user8 → user1
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 8;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 1;
    INSERT INTO friendship (id, requester_id, receiver_id, status, created_at, updated_at)
    VALUES (UUID(), v_user1_id, v_user2_id, 'REJECTED', NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 24 DAY);

    -- =========================================================
    -- 13) chat_room 8개 (친구 관계인 유저 쌍)
    -- =========================================================
    DROP TEMPORARY TABLE IF EXISTS tmp_chat_rooms;
    CREATE TEMPORARY TABLE tmp_chat_rooms (id CHAR(36) PRIMARY KEY, user1_id CHAR(36), user2_id CHAR(36), rn INT);

    -- user1 ↔ user2
    SET v_room_id = UUID();
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 1;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 2;
    INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
    VALUES (v_room_id, v_user1_id, v_user2_id, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 10 MINUTE);
    INSERT INTO tmp_chat_rooms VALUES (v_room_id, v_user1_id, v_user2_id, 1);

    -- user1 ↔ user3
    SET v_room_id = UUID();
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 1;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 3;
    INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
    VALUES (v_room_id, v_user1_id, v_user2_id, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 2 HOUR);
    INSERT INTO tmp_chat_rooms VALUES (v_room_id, v_user1_id, v_user2_id, 2);

    -- user1 ↔ user4
    SET v_room_id = UUID();
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 1;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 4;
    INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
    VALUES (v_room_id, v_user1_id, v_user2_id, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 1 DAY);
    INSERT INTO tmp_chat_rooms VALUES (v_room_id, v_user1_id, v_user2_id, 3);

    -- user2 ↔ user3
    SET v_room_id = UUID();
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 2;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 3;
    INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
    VALUES (v_room_id, v_user1_id, v_user2_id, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 5 HOUR);
    INSERT INTO tmp_chat_rooms VALUES (v_room_id, v_user1_id, v_user2_id, 4);

    -- user3 ↔ user6
    SET v_room_id = UUID();
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 3;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 6;
    INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
    VALUES (v_room_id, v_user1_id, v_user2_id, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 3 DAY);
    INSERT INTO tmp_chat_rooms VALUES (v_room_id, v_user1_id, v_user2_id, 5);

    -- user1 ↔ user5
    SET v_room_id = UUID();
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 1;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 5;
    INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
    VALUES (v_room_id, v_user1_id, v_user2_id, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 30 MINUTE);
    INSERT INTO tmp_chat_rooms VALUES (v_room_id, v_user1_id, v_user2_id, 6);

    -- user2 ↔ user4
    SET v_room_id = UUID();
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 2;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 4;
    INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
    VALUES (v_room_id, v_user1_id, v_user2_id, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 4 HOUR);
    INSERT INTO tmp_chat_rooms VALUES (v_room_id, v_user1_id, v_user2_id, 7);

    -- user4 ↔ user7
    SET v_room_id = UUID();
    SELECT id INTO v_user1_id FROM tmp_users WHERE rn = 4;
    SELECT id INTO v_user2_id FROM tmp_users WHERE rn = 7;
    INSERT INTO chat_room (id, user1_id, user2_id, created_at, last_message_at)
    VALUES (v_room_id, v_user1_id, v_user2_id, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 6 HOUR);
    INSERT INTO tmp_chat_rooms VALUES (v_room_id, v_user1_id, v_user2_id, 8);

    -- =========================================================
    -- 14) chat_message 대화 내용
    -- =========================================================

    -- 채팅방 1: user1 ↔ user2 (활발한 일상 대화, 30개)
    BEGIN
        DECLARE v_msgs JSON;
        DECLARE v_rm_id CHAR(36);
        DECLARE v_u1 CHAR(36);
        DECLARE v_u2 CHAR(36);

        SELECT id, user1_id, user2_id INTO v_rm_id, v_u1, v_u2 FROM tmp_chat_rooms WHERE rn = 1;
        SET v_msgs = JSON_ARRAY(
            '안녕! 오늘 뭐 했어?', '나는 운동하고 왔어 ㅋㅋ', '오 대단하다! 나도 해야 하는데',
            '같이 하자! 내일 시간 돼?', '내일은 좀 바쁘고 모레는 괜찮아', '그래 모레 저녁에 보자',
            '좋아! 그때 봐', '응응 기대된다!', '아 참 오늘 점심에 새로 생긴 파스타집 갔는데 대박이었어',
            '어디? 나도 가보고 싶다', '역삼역 3번 출구 쪽이야 이름이 뭐더라... 파스타로드?', '오 검색해볼게 고마워!',
            '근데 요즘 넷플릭스에 볼 거 있어?', '어제 새로 나온 드라마 봤는데 괜찮더라', '무슨 드라마? 추천해줘',
            '제목이 기억이 안 나는데 찾아서 보내줄게', 'ㅋㅋ 빨리 보내줘', '찾았다! 링크 보낼게',
            '오 고마워 오늘 저녁에 볼게', '재밌으면 알려줘 ㅋㅋ', '아 그리고 다음 주 금요일에 모임 있대',
            '진짜? 누가 주최하는 건데?', '민수가 한다고 장소는 강남 쪽이래', '오 좋다 나도 갈게!',
            '그럼 같이 가자 내가 차 끌고 갈게', '역시 든든하다 ㅋㅋ', '몇 시쯤 출발하면 될까?',
            '6시쯤?', '그래 6시에 보자!', '알겠어 그때 봐!'
        );
        SET i = 0;
        WHILE i < 30 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (UUID(), v_rm_id,
                    IF(i % 2 = 0, v_u1, v_u2),
                    JSON_UNQUOTE(JSON_EXTRACT(v_msgs, CONCAT('$[', i, ']'))),
                    IF(i < 24, 1, 0),
                    NOW() - INTERVAL 3 DAY + INTERVAL (i * 12) MINUTE);
            SET i = i + 1;
        END WHILE;
    END;

    -- 채팅방 2: user1 ↔ user3 (프로젝트 협업, 24개)
    BEGIN
        DECLARE v_msgs JSON;
        DECLARE v_rm_id CHAR(36);
        DECLARE v_u1 CHAR(36);
        DECLARE v_u2 CHAR(36);

        SELECT id, user1_id, user2_id INTO v_rm_id, v_u1, v_u2 FROM tmp_chat_rooms WHERE rn = 2;
        SET v_msgs = JSON_ARRAY(
            '프로젝트 진행 어때?', '거의 다 했어! 내일 공유할게', '좋아 기대된다',
            '혹시 디자인 쪽 의견 있어?', '깔끔하게 가는 게 좋을 것 같아', '알겠어 참고할게!',
            'API 명세서 정리했는데 한번 봐줄 수 있어?', '응 지금 보내줘', '노션에 올려놨어 링크 확인해봐',
            '봤어! 전체적으로 좋은데 인증 쪽 수정이 좀 필요할 것 같아', '어떤 부분?',
            '토큰 갱신 로직이 빠져있어 추가하면 좋겠어', '아 맞다 그거 까먹었네 바로 반영할게',
            '고마워! 그리고 테스트 코드도 좀 추가하자', '동의 이번 주 안에 커버리지 70% 이상 맞추자',
            '좋아 나는 서비스 레이어 테스트 할게', '그럼 나는 컨트롤러 쪽 할게', '완벽해 빨리 끝내고 배포하자',
            '배포는 금요일 목표로 하면 될까?', '응 금요일이면 충분할 것 같아', 'CI/CD 파이프라인은 내가 세팅해놨어',
            '오 벌써? 역시 일 빠르다', '아니야 ㅋㅋ 그냥 예전 거 재활용한 거야', '어쨌든 고마워 이번 프로젝트 잘 마무리하자!'
        );
        SET i = 0;
        WHILE i < 24 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (UUID(), v_rm_id,
                    IF(i % 2 = 0, v_u1, v_u2),
                    JSON_UNQUOTE(JSON_EXTRACT(v_msgs, CONCAT('$[', i, ']'))),
                    IF(i < 18, 1, 0),
                    NOW() - INTERVAL 5 HOUR + INTERVAL (i * 10) MINUTE);
            SET i = i + 1;
        END WHILE;
    END;

    -- 채팅방 3: user1 ↔ user4 (약속 잡기, 20개)
    BEGIN
        DECLARE v_msgs JSON;
        DECLARE v_rm_id CHAR(36);
        DECLARE v_u1 CHAR(36);
        DECLARE v_u2 CHAR(36);

        SELECT id, user1_id, user2_id INTO v_rm_id, v_u1, v_u2 FROM tmp_chat_rooms WHERE rn = 3;
        SET v_msgs = JSON_ARRAY(
            '주말에 뭐 할 거야?', '아직 계획 없어, 왜?', '같이 밥 먹자!', '좋아 어디서 볼까?',
            '홍대 쪽 어때?', '홍대 좋지! 뭐 먹을까?', '고기 먹고 싶다 ㅋㅋ', '오 나도! 삼겹살 ㄱㄱ',
            '좋아 내가 맛집 찾아볼게', '응 예약도 해줘 ㅋㅋ', '알겠어 토요일 저녁 7시 괜찮아?', '완벽해!',
            '예약했어! 홍대역 2번 출구에서 만나자', '오키 거기서 봐', '아 참 주영이도 같이 간대',
            '오 좋아 오랜만이다', '셋이서 신나게 먹자 ㅋㅋ', '기대된다 빨리 토요일 왔으면',
            '나도 ㅋㅋ 이번 주 너무 길다', '조금만 참자! 곧이야'
        );
        SET i = 0;
        WHILE i < 20 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (UUID(), v_rm_id,
                    IF(i % 2 = 0, v_u1, v_u2),
                    JSON_UNQUOTE(JSON_EXTRACT(v_msgs, CONCAT('$[', i, ']'))),
                    IF(i < 16, 1, 0),
                    NOW() - INTERVAL 1 DAY + INTERVAL (i * 15) MINUTE);
            SET i = i + 1;
        END WHILE;
    END;

    -- 채팅방 4: user2 ↔ user3 (책 추천 & 스터디, 22개)
    BEGIN
        DECLARE v_msgs JSON;
        DECLARE v_rm_id CHAR(36);
        DECLARE v_u1 CHAR(36);
        DECLARE v_u2 CHAR(36);

        SELECT id, user1_id, user2_id INTO v_rm_id, v_u1, v_u2 FROM tmp_chat_rooms WHERE rn = 4;
        SET v_msgs = JSON_ARRAY(
            '저번에 알려준 책 다 읽었어', '어땠어? 재밌었어?', '진짜 좋았어! 추천 고마워',
            '다행이다 ㅎㅎ 다음 책도 추천해줄까?', '응 부탁해!', '클린 코드 읽어봤어?',
            '아직 안 읽었어 좋아?', '개발자 필독서야 꼭 읽어봐', '오 바로 주문할게',
            '그리고 같이 스터디 할래?', '무슨 스터디?', '알고리즘 스터디 주 1회 정도',
            '좋아! 요일은 언제가 좋아?', '수요일 저녁 어때?', '수요일 괜찮아 온라인으로?',
            '응 디스코드로 하자', '좋아 방 만들어줘', '만들었어 초대 링크 보낼게',
            '고마워 들어갔어!', '그럼 이번 주 수요일부터 시작하자', '알겠어 첫 주제는 뭘로 할까?',
            '그래프 탐색부터 하자 BFS/DFS'
        );
        SET i = 0;
        WHILE i < 22 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (UUID(), v_rm_id,
                    IF(i % 2 = 0, v_u1, v_u2),
                    JSON_UNQUOTE(JSON_EXTRACT(v_msgs, CONCAT('$[', i, ']'))),
                    IF(i < 16, 1, 0),
                    NOW() - INTERVAL 8 HOUR + INTERVAL (i * 15) MINUTE);
            SET i = i + 1;
        END WHILE;
    END;

    -- 채팅방 5: user3 ↔ user6 (안부 & 근황, 18개)
    BEGIN
        DECLARE v_msgs JSON;
        DECLARE v_rm_id CHAR(36);
        DECLARE v_u1 CHAR(36);
        DECLARE v_u2 CHAR(36);

        SELECT id, user1_id, user2_id INTO v_rm_id, v_u1, v_u2 FROM tmp_chat_rooms WHERE rn = 5;
        SET v_msgs = JSON_ARRAY(
            '요즘 어떻게 지내?', '바쁘지만 잘 지내고 있어!', '나중에 시간 되면 만나자', '그래 꼭!',
            '요즘 회사는 어때?', '프로젝트가 좀 바쁜데 나름 재밌어', '뭐 만들고 있는데?',
            '모바일 앱 리뉴얼 중이야', '오 멋지다! 출시되면 알려줘', '응 꼭! 그쪽은 뭐 하고 있어?',
            '나는 이직 준비 중이야', '오 진짜? 어디 쪽으로?', '백엔드 쪽으로 좀 더 큰 회사 보고 있어',
            '대박 응원한다! 필요한 거 있으면 말해', '고마워 면접 팁 좀 알려줘 ㅋㅋ',
            '시스템 디자인이 제일 중요해 거기 집중해봐', '알겠어 자료 좀 공유해줄 수 있어?',
            '응 정리해서 보내줄게!'
        );
        SET i = 0;
        WHILE i < 18 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (UUID(), v_rm_id,
                    IF(i % 2 = 0, v_u1, v_u2),
                    JSON_UNQUOTE(JSON_EXTRACT(v_msgs, CONCAT('$[', i, ']'))),
                    IF(i < 14, 1, 0),
                    NOW() - INTERVAL 3 DAY + INTERVAL (i * 20) MINUTE);
            SET i = i + 1;
        END WHILE;
    END;

    -- 채팅방 6: user1 ↔ user5 (운동 메이트, 26개)
    BEGIN
        DECLARE v_msgs JSON;
        DECLARE v_rm_id CHAR(36);
        DECLARE v_u1 CHAR(36);
        DECLARE v_u2 CHAR(36);

        SELECT id, user1_id, user2_id INTO v_rm_id, v_u1, v_u2 FROM tmp_chat_rooms WHERE rn = 6;
        SET v_msgs = JSON_ARRAY(
            '오늘 헬스장 갈 거야?', '응 7시쯤 갈 생각이야', '나도 갈게 같이 하자!',
            '좋아 오늘 하체 하는 날이지?', '응 스쿼트 빡세게 하자 ㅋㅋ', '아 다리 벌써 아프다',
            'ㅋㅋㅋ 안 하면 더 아파', '맞아 가자가자', '아 그리고 프로틴 다 떨어졌는데 추천해줄 거 있어?',
            '나 마이프로틴 초코맛 먹는데 괜찮아', '맛있어? 뒷맛은?', '나쁘지 않아 가성비 최고임',
            '오 그러면 같이 주문하자 배송비 아끼게', 'ㅋㅋ 좋아 내가 장바구니 공유할게',
            '고마워! 아 근데 오늘 벤치프레스 PR 깰 것 같아', '오 지금 얼마야?',
            '80kg인데 오늘 85 도전해볼 거야', '대박 나 보조 서줄게!', '든든하다 ㅋㅋ',
            '아 그리고 다음 달에 바디프로필 찍을 건데 같이 할래?', '바디프로필? 나도 해보고 싶었어!',
            '그럼 같이 준비하자 식단도 맞추고', '좋아 월요일부터 시작하자', '닭가슴살 질릴 각오해 ㅋㅋ',
            '이미 각오했어 ㅋㅋㅋ', '그래 파이팅!'
        );
        SET i = 0;
        WHILE i < 26 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (UUID(), v_rm_id,
                    IF(i % 2 = 0, v_u1, v_u2),
                    JSON_UNQUOTE(JSON_EXTRACT(v_msgs, CONCAT('$[', i, ']'))),
                    IF(i < 20, 1, 0),
                    NOW() - INTERVAL 2 DAY + INTERVAL (i * 10) MINUTE);
            SET i = i + 1;
        END WHILE;
    END;

    -- 채팅방 7: user2 ↔ user4 (게임 & 취미, 20개)
    BEGIN
        DECLARE v_msgs JSON;
        DECLARE v_rm_id CHAR(36);
        DECLARE v_u1 CHAR(36);
        DECLARE v_u2 CHAR(36);

        SELECT id, user1_id, user2_id INTO v_rm_id, v_u1, v_u2 FROM tmp_chat_rooms WHERE rn = 7;
        SET v_msgs = JSON_ARRAY(
            '오늘 저녁에 롤 할래?', '좋아! 몇 시에?', '9시쯤 어때?', '오키 칼 접속할게',
            '듀오 가자 요즘 랭크 어때?', '골드에서 못 올라가고 있어 ㅠ', 'ㅋㅋ 내가 캐리해줄게',
            '진짜? 너 플래 갔다며', '응 어제 승급했어', '부럽다 나도 빨리 올라가고 싶다',
            '서포터로 하면 금방 올라가', '서포터 싫어 ㅋㅋ 딜 하고 싶어',
            '그럼 원딜 해 내가 서포터 할게', '오 고마워 조합 미쳤다', '아 근데 새 패치 봤어?',
            '응 챔피언 밸런스 또 바뀌었더라', '내 원챔 버프됐어 ㅋㅋㅋ', '럭키 ㅋㅋ 내 캐릭은 너프됐는데',
            '안습이다 ㅋㅋ', '어쨌든 오늘 저녁에 보자!'
        );
        SET i = 0;
        WHILE i < 20 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (UUID(), v_rm_id,
                    IF(i % 2 = 0, v_u1, v_u2),
                    JSON_UNQUOTE(JSON_EXTRACT(v_msgs, CONCAT('$[', i, ']'))),
                    IF(i < 14, 1, 0),
                    NOW() - INTERVAL 6 HOUR + INTERVAL (i * 12) MINUTE);
            SET i = i + 1;
        END WHILE;
    END;

    -- 채팅방 8: user4 ↔ user7 (직장 동료, 16개)
    BEGIN
        DECLARE v_msgs JSON;
        DECLARE v_rm_id CHAR(36);
        DECLARE v_u1 CHAR(36);
        DECLARE v_u2 CHAR(36);

        SELECT id, user1_id, user2_id INTO v_rm_id, v_u1, v_u2 FROM tmp_chat_rooms WHERE rn = 8;
        SET v_msgs = JSON_ARRAY(
            '오늘 회의 몇 시였지?', '2시야 회의실 B', '고마워 자료 준비해야 하는데',
            '내가 어제 정리해둔 거 공유해줄까?', '아 진짜? 부탁해!', '메일로 보냈어 확인해봐',
            '받았어 감사합니다!', '별말씀을 ㅋㅋ', '점심 뭐 먹을까?',
            '회사 앞에 새로 생긴 국밥집 가볼까?', '국밥 좋지 가자!', '12시에 로비에서 만나',
            '아 그리고 내일 재택이지?', '응 내일 재택이야', '그럼 슬랙으로 연락하자', '알겠어 내일 봐!'
        );
        SET i = 0;
        WHILE i < 16 DO
            INSERT INTO chat_message (id, chat_room_id, sender_id, content, is_read, created_at)
            VALUES (UUID(), v_rm_id,
                    IF(i % 2 = 0, v_u1, v_u2),
                    JSON_UNQUOTE(JSON_EXTRACT(v_msgs, CONCAT('$[', i, ']'))),
                    IF(i < 12, 1, 0),
                    NOW() - INTERVAL 8 HOUR + INTERVAL (i * 18) MINUTE);
            SET i = i + 1;
        END WHILE;
    END;

    -- =========================================================
    -- 임시 테이블 정리
    -- =========================================================
    DROP TEMPORARY TABLE IF EXISTS tmp_users;
    DROP TEMPORARY TABLE IF EXISTS tmp_categories;
    DROP TEMPORARY TABLE IF EXISTS tmp_chat_rooms;
    DROP TEMPORARY TABLE IF EXISTS tmp_level1;
    DROP TEMPORARY TABLE IF EXISTS tmp_level2;
    DROP TEMPORARY TABLE IF EXISTS tmp_level3;
    DROP TEMPORARY TABLE IF EXISTS tmp_level4;
    DROP TEMPORARY TABLE IF EXISTS tmp_level5;

END$$

DELIMITER ;

-- 프로시저 실행
CALL generate_dummy_data();

-- 프로시저 삭제
DROP PROCEDURE IF EXISTS generate_dummy_data;

-- =========================================================
-- 결과 확인
-- =========================================================
SELECT 'users' AS `table`, COUNT(*) AS count FROM users
UNION ALL SELECT 'category', COUNT(*) FROM category
UNION ALL SELECT 'category_like', COUNT(*) FROM category_like
UNION ALL SELECT 'comment', COUNT(*) FROM `comment`
UNION ALL SELECT 'check_record', COUNT(*) FROM check_record
UNION ALL SELECT 'check_list_record', COUNT(*) FROM check_list_record
UNION ALL SELECT 'text_record', COUNT(*) FROM text_record
UNION ALL SELECT 'time_record', COUNT(*) FROM time_record
UNION ALL SELECT 'number_record', COUNT(*) FROM number_record
UNION ALL SELECT 'expense_record', COUNT(*) FROM expense_record
UNION ALL SELECT 'expense_tag', COUNT(*) FROM expense_tag
UNION ALL SELECT 'friendship', COUNT(*) FROM friendship
UNION ALL SELECT 'chat_room', COUNT(*) FROM chat_room
UNION ALL SELECT 'chat_message', COUNT(*) FROM chat_message;
