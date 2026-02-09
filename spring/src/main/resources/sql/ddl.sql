-- =========================================================
-- Keep4Life DDL
-- MySQL 8.0
-- 실행: mysql -u root -p app < ddl.sql
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 기존 테이블 삭제 (의존성 역순)
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS chat_room;
DROP TABLE IF EXISTS friendship;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS expense_record_tag;
DROP TABLE IF EXISTS expense_record;
DROP TABLE IF EXISTS expense_tag;
DROP TABLE IF EXISTS check_list_record;
DROP TABLE IF EXISTS check_record;
DROP TABLE IF EXISTS number_record;
DROP TABLE IF EXISTS text_record_image;
DROP TABLE IF EXISTS text_record;
DROP TABLE IF EXISTS time_record;
DROP TABLE IF EXISTS category_like;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1) users
-- =========================================================
CREATE TABLE users (
    id                CHAR(36)     NOT NULL,
    email             varchar(255),
    username          varchar(255),
    nickname          varchar(255),
    password          text,
    profile_image_url varchar(255),
    title             varchar(255),
    description       text,
    authority         smallint,
    birth_year        integer      NOT NULL,
    birth_month       integer      NOT NULL,
    birth_day         integer      NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 2) category
-- =========================================================
CREATE TABLE category (
    id          CHAR(36)       NOT NULL,
    user_id     CHAR(36),
    title       varchar(255),
    description text,
    record_type varchar(255),
    visibility  varchar(255)   NOT NULL,
    created_at  DATETIME(6)    NOT NULL,
    CONSTRAINT category_pkey PRIMARY KEY (id),
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT category_record_type_check
        CHECK (record_type IN ('CHECK', 'CHECKLIST', 'TEXT', 'TIME', 'NUMBER', 'EXPENSE')),
    CONSTRAINT category_visibility_check
        CHECK (visibility IN ('PRIVATE', 'PUBLIC'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 3) category_like
-- =========================================================
CREATE TABLE category_like (
    id          CHAR(36) NOT NULL,
    user_id     CHAR(36),
    category_id CHAR(36),
    created_at  DATETIME(6),
    CONSTRAINT category_like_pkey PRIMARY KEY (id),
    CONSTRAINT uk_user_category UNIQUE (user_id, category_id),
    CONSTRAINT fk_category_like_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_category_like_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 4) comment
-- =========================================================
CREATE TABLE `comment` (
    id          CHAR(36) NOT NULL,
    comment     varchar(255),
    parent_id   CHAR(36),
    category_id CHAR(36),
    user_id     CHAR(36),
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6),
    CONSTRAINT comment_pkey PRIMARY KEY (id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES `comment` (id),
    CONSTRAINT fk_comment_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 5) text_record
-- =========================================================
CREATE TABLE text_record (
    id          CHAR(36) NOT NULL,
    category_id CHAR(36) NOT NULL,
    title       varchar(255),
    text        text,
    date        date,
    CONSTRAINT text_record_pkey PRIMARY KEY (id),
    CONSTRAINT fk_text_record_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 6) text_record_image
-- =========================================================
CREATE TABLE text_record_image (
    id             CHAR(36) NOT NULL,
    text_record_id CHAR(36) NOT NULL,
    image_url      varchar(255),
    display_order  integer  NOT NULL,
    CONSTRAINT text_record_image_pkey PRIMARY KEY (id),
    CONSTRAINT fk_text_record_image_record FOREIGN KEY (text_record_id) REFERENCES text_record (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 7) check_record
-- =========================================================
CREATE TABLE check_record (
    id          CHAR(36) NOT NULL,
    category_id CHAR(36),
    success     boolean  NOT NULL,
    date        date,
    CONSTRAINT check_record_pkey PRIMARY KEY (id),
    CONSTRAINT uk_category_date UNIQUE (category_id, date),
    CONSTRAINT fk_check_record_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 8) check_list_record
-- =========================================================
CREATE TABLE check_list_record (
    id          CHAR(36)     NOT NULL,
    category_id CHAR(36),
    todo        varchar(255),
    success     boolean      NOT NULL,
    date        date,
    CONSTRAINT check_list_record_pkey PRIMARY KEY (id),
    CONSTRAINT fk_check_list_record_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 9) time_record
-- =========================================================
CREATE TABLE time_record (
    id          CHAR(36) NOT NULL,
    category_id CHAR(36),
    time        TIME(6),
    date        date,
    CONSTRAINT time_record_pkey PRIMARY KEY (id),
    CONSTRAINT uk_time_record_category_date UNIQUE (category_id, date),
    CONSTRAINT fk_time_record_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 10) number_record
-- =========================================================
CREATE TABLE number_record (
    id          CHAR(36) NOT NULL,
    category_id CHAR(36),
    number      DOUBLE   NOT NULL,
    date        date,
    CONSTRAINT number_record_pkey PRIMARY KEY (id),
    CONSTRAINT uk_number_record_category_date UNIQUE (category_id, date),
    CONSTRAINT fk_number_record_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 11) expense_tag
-- =========================================================
CREATE TABLE expense_tag (
    id      CHAR(36) NOT NULL,
    user_id CHAR(36),
    name    varchar(255),
    CONSTRAINT expense_tag_pkey PRIMARY KEY (id),
    CONSTRAINT fk_expense_tag_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 12) expense_record
-- =========================================================
CREATE TABLE expense_record (
    id             CHAR(36) NOT NULL,
    category_id    CHAR(36),
    amount         DOUBLE   NOT NULL,
    expense_type   varchar(255),
    payment_method varchar(255),
    merchant       varchar(255),
    memo           varchar(255),
    date           date,
    CONSTRAINT expense_record_pkey PRIMARY KEY (id),
    CONSTRAINT fk_expense_record_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT expense_record_expense_type_check
        CHECK (expense_type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT expense_record_payment_method_check
        CHECK (payment_method IN ('CASH', 'CARD', 'TRANSFER', 'OTHER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 13) expense_record_tag
-- =========================================================
CREATE TABLE expense_record_tag (
    id                CHAR(36) NOT NULL,
    expense_record_id CHAR(36),
    expense_tag_id    CHAR(36),
    CONSTRAINT expense_record_tag_pkey PRIMARY KEY (id),
    CONSTRAINT fk_expense_record_tag_record FOREIGN KEY (expense_record_id) REFERENCES expense_record (id),
    CONSTRAINT fk_expense_record_tag_tag FOREIGN KEY (expense_tag_id) REFERENCES expense_tag (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 14) notification
-- =========================================================
CREATE TABLE notification (
    id          CHAR(36)    NOT NULL,
    receiver_id CHAR(36)    NOT NULL,
    sender_id   CHAR(36)    NOT NULL,
    type        varchar(255) NOT NULL,
    data        JSON,
    `read`      boolean     NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    CONSTRAINT notification_pkey PRIMARY KEY (id),
    CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES users (id),
    CONSTRAINT fk_notification_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT notification_type_check
        CHECK (type IN ('COMMENT', 'LIKE', 'REPLY', 'FRIEND_REQUEST', 'FRIEND_ACCEPT', 'CHAT_MESSAGE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 15) friendship
-- =========================================================
CREATE TABLE friendship (
    id           CHAR(36)    NOT NULL,
    requester_id CHAR(36)    NOT NULL,
    receiver_id  CHAR(36)    NOT NULL,
    status       varchar(255) NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6),
    CONSTRAINT friendship_pkey PRIMARY KEY (id),
    CONSTRAINT uk_requester_receiver UNIQUE (requester_id, receiver_id),
    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_friendship_receiver FOREIGN KEY (receiver_id) REFERENCES users (id),
    CONSTRAINT friendship_status_check
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 16) chat_room
-- =========================================================
CREATE TABLE chat_room (
    id              CHAR(36)    NOT NULL,
    user1_id        CHAR(36)    NOT NULL,
    user2_id        CHAR(36)    NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    last_message_at DATETIME(6),
    CONSTRAINT chat_room_pkey PRIMARY KEY (id),
    CONSTRAINT uk_chat_room_users UNIQUE (user1_id, user2_id),
    CONSTRAINT fk_chat_room_user1 FOREIGN KEY (user1_id) REFERENCES users (id),
    CONSTRAINT fk_chat_room_user2 FOREIGN KEY (user2_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- 17) chat_message
-- =========================================================
CREATE TABLE chat_message (
    id           CHAR(36)    NOT NULL,
    chat_room_id CHAR(36)    NOT NULL,
    sender_id    CHAR(36)    NOT NULL,
    content      text        NOT NULL,
    is_read      boolean     NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    CONSTRAINT chat_message_pkey PRIMARY KEY (id),
    CONSTRAINT fk_chat_message_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
