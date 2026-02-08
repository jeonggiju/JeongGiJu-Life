-- =========================================================
-- Keep4Life DDL
-- PostgreSQL 16
-- 실행: psql -U app -d appdb -f ddl.sql
-- =========================================================

-- 기존 테이블 삭제 (의존성 역순)
DROP TABLE IF EXISTS chat_message CASCADE;
DROP TABLE IF EXISTS chat_room CASCADE;
DROP TABLE IF EXISTS friendship CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS expense_record_tag CASCADE;
DROP TABLE IF EXISTS expense_record CASCADE;
DROP TABLE IF EXISTS expense_tag CASCADE;
DROP TABLE IF EXISTS check_list_record CASCADE;
DROP TABLE IF EXISTS check_record CASCADE;
DROP TABLE IF EXISTS number_record CASCADE;
DROP TABLE IF EXISTS text_record_image CASCADE;
DROP TABLE IF EXISTS text_record CASCADE;
DROP TABLE IF EXISTS time_record CASCADE;
DROP TABLE IF EXISTS category_like CASCADE;
DROP TABLE IF EXISTS "comment" CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- 1) users
-- =========================================================
CREATE TABLE users (
    id                uuid         NOT NULL,
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
);

-- =========================================================
-- 2) category
-- =========================================================
CREATE TABLE category (
    id          uuid                           NOT NULL,
    user_id     uuid,
    title       varchar(255),
    description text,
    record_type varchar(255),
    visibility  varchar(255)                   NOT NULL,
    created_at  timestamp(6) without time zone NOT NULL,
    CONSTRAINT category_pkey PRIMARY KEY (id),
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT category_record_type_check
        CHECK (record_type IN ('CHECK', 'CHECKLIST', 'TEXT', 'TIME', 'NUMBER', 'EXPENSE')),
    CONSTRAINT category_visibility_check
        CHECK (visibility IN ('PRIVATE', 'PUBLIC'))
);

-- =========================================================
-- 3) category_like
-- =========================================================
CREATE TABLE category_like (
    id          uuid NOT NULL,
    user_id     uuid,
    category_id uuid,
    created_at  timestamp(6) without time zone,
    CONSTRAINT category_like_pkey PRIMARY KEY (id),
    CONSTRAINT uk_user_category UNIQUE (user_id, category_id),
    CONSTRAINT fk_category_like_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_category_like_category FOREIGN KEY (category_id) REFERENCES category (id)
);

-- =========================================================
-- 4) comment
-- =========================================================
CREATE TABLE "comment" (
    id          uuid NOT NULL,
    comment     varchar(255),
    parent_id   uuid,
    category_id uuid,
    user_id     uuid,
    created_at  timestamp(6) without time zone NOT NULL,
    updated_at  timestamp(6) without time zone,
    CONSTRAINT comment_pkey PRIMARY KEY (id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES "comment" (id),
    CONSTRAINT fk_comment_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =========================================================
-- 5) text_record
-- =========================================================
CREATE TABLE text_record (
    id          uuid NOT NULL,
    category_id uuid NOT NULL,
    title       varchar(255),
    text        text,
    date        date,
    CONSTRAINT text_record_pkey PRIMARY KEY (id),
    CONSTRAINT fk_text_record_category FOREIGN KEY (category_id) REFERENCES category (id)
);

-- =========================================================
-- 6) text_record_image
-- =========================================================
CREATE TABLE text_record_image (
    id             uuid    NOT NULL,
    text_record_id uuid    NOT NULL,
    image_url      varchar(255),
    display_order  integer NOT NULL,
    CONSTRAINT text_record_image_pkey PRIMARY KEY (id),
    CONSTRAINT fk_text_record_image_record FOREIGN KEY (text_record_id) REFERENCES text_record (id)
);

-- =========================================================
-- 7) check_record
-- =========================================================
CREATE TABLE check_record (
    id          uuid    NOT NULL,
    category_id uuid,
    success     boolean NOT NULL,
    date        date,
    CONSTRAINT check_record_pkey PRIMARY KEY (id),
    CONSTRAINT uk_category_date UNIQUE (category_id, date),
    CONSTRAINT fk_check_record_category FOREIGN KEY (category_id) REFERENCES category (id)
);

-- =========================================================
-- 8) check_list_record
-- =========================================================
CREATE TABLE check_list_record (
    id          uuid    NOT NULL,
    category_id uuid,
    todo        varchar(255),
    success     boolean NOT NULL,
    date        date,
    CONSTRAINT check_list_record_pkey PRIMARY KEY (id),
    CONSTRAINT fk_check_list_record_category FOREIGN KEY (category_id) REFERENCES category (id)
);

-- =========================================================
-- 9) time_record
-- =========================================================
CREATE TABLE time_record (
    id          uuid NOT NULL,
    category_id uuid,
    time        time(6) without time zone,
    date        date,
    CONSTRAINT time_record_pkey PRIMARY KEY (id),
    CONSTRAINT uk_time_record_category_date UNIQUE (category_id, date),
    CONSTRAINT fk_time_record_category FOREIGN KEY (category_id) REFERENCES category (id)
);

-- =========================================================
-- 10) number_record
-- =========================================================
CREATE TABLE number_record (
    id          uuid NOT NULL,
    category_id uuid,
    number      float8 NOT NULL,
    date        date,
    CONSTRAINT number_record_pkey PRIMARY KEY (id),
    CONSTRAINT uk_number_record_category_date UNIQUE (category_id, date),
    CONSTRAINT fk_number_record_category FOREIGN KEY (category_id) REFERENCES category (id)
);

-- =========================================================
-- 11) expense_tag
-- =========================================================
CREATE TABLE expense_tag (
    id      uuid NOT NULL,
    user_id uuid,
    name    varchar(255),
    CONSTRAINT expense_tag_pkey PRIMARY KEY (id),
    CONSTRAINT fk_expense_tag_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =========================================================
-- 12) expense_record
-- =========================================================
CREATE TABLE expense_record (
    id             uuid   NOT NULL,
    category_id    uuid,
    amount         float8 NOT NULL,
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
);

-- =========================================================
-- 13) expense_record_tag
-- =========================================================
CREATE TABLE expense_record_tag (
    id                uuid NOT NULL,
    expense_record_id uuid,
    expense_tag_id    uuid,
    CONSTRAINT expense_record_tag_pkey PRIMARY KEY (id),
    CONSTRAINT fk_expense_record_tag_record FOREIGN KEY (expense_record_id) REFERENCES expense_record (id),
    CONSTRAINT fk_expense_record_tag_tag FOREIGN KEY (expense_tag_id) REFERENCES expense_tag (id)
);

-- =========================================================
-- 14) notification
-- =========================================================
CREATE TABLE notification (
    id          uuid                           NOT NULL,
    receiver_id uuid                           NOT NULL,
    sender_id   uuid                           NOT NULL,
    type        varchar(255)                   NOT NULL,
    data        jsonb,
    read        boolean                        NOT NULL,
    created_at  timestamp(6) without time zone NOT NULL,
    CONSTRAINT notification_pkey PRIMARY KEY (id),
    CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES users (id),
    CONSTRAINT fk_notification_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT notification_type_check
        CHECK (type IN ('COMMENT', 'LIKE', 'REPLY', 'FRIEND_REQUEST', 'FRIEND_ACCEPT', 'CHAT_MESSAGE'))
);

-- =========================================================
-- 15) friendship
-- =========================================================
CREATE TABLE friendship (
    id           uuid                           NOT NULL,
    requester_id uuid                           NOT NULL,
    receiver_id  uuid                           NOT NULL,
    status       varchar(255)                   NOT NULL,
    created_at   timestamp(6) without time zone NOT NULL,
    updated_at   timestamp(6) without time zone,
    CONSTRAINT friendship_pkey PRIMARY KEY (id),
    CONSTRAINT uk_requester_receiver UNIQUE (requester_id, receiver_id),
    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_friendship_receiver FOREIGN KEY (receiver_id) REFERENCES users (id),
    CONSTRAINT friendship_status_check
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED'))
);

-- =========================================================
-- 16) chat_room
-- =========================================================
CREATE TABLE chat_room (
    id              uuid                           NOT NULL,
    user1_id        uuid                           NOT NULL,
    user2_id        uuid                           NOT NULL,
    created_at      timestamp(6) without time zone NOT NULL,
    last_message_at timestamp(6) without time zone,
    CONSTRAINT chat_room_pkey PRIMARY KEY (id),
    CONSTRAINT uk_chat_room_users UNIQUE (user1_id, user2_id),
    CONSTRAINT fk_chat_room_user1 FOREIGN KEY (user1_id) REFERENCES users (id),
    CONSTRAINT fk_chat_room_user2 FOREIGN KEY (user2_id) REFERENCES users (id)
);

-- =========================================================
-- 17) chat_message
-- =========================================================
CREATE TABLE chat_message (
    id           uuid                           NOT NULL,
    chat_room_id uuid                           NOT NULL,
    sender_id    uuid                           NOT NULL,
    content      text                           NOT NULL,
    is_read      boolean                        NOT NULL,
    created_at   timestamp(6) without time zone NOT NULL,
    CONSTRAINT chat_message_pkey PRIMARY KEY (id),
    CONSTRAINT fk_chat_message_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);
