-- DROP SCHEMA public;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

-- DROP SEQUENCE public.batch_job_execution_seq;

CREATE SEQUENCE public.batch_job_execution_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE public.batch_job_seq;

CREATE SEQUENCE public.batch_job_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE public.batch_step_execution_seq;

CREATE SEQUENCE public.batch_step_execution_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
                              authority int2 NULL,
                              birth_day int4 NULL,
                              birth_month int4 NULL,
                              birth_year int4 NULL,
                              id uuid NOT NULL,
                              description varchar(255) NULL,
                              email varchar(255) NULL,
                              "password" text NULL,
                              title varchar(255) NULL,
                              username varchar(255) NULL,
                              CONSTRAINT users_authority_check CHECK (((authority >= 0) AND (authority <= 1))),
                              CONSTRAINT users_email_key UNIQUE (email),
                              CONSTRAINT users_pkey PRIMARY KEY (id)
);


-- public.category definition

-- Drop table

-- DROP TABLE public.category;

CREATE TABLE public.category (
                                 id uuid NOT NULL,
                                 user_id uuid NULL,
                                 description text NULL,
                                 record_type varchar(255) NULL,
                                 title varchar(255) NULL,
                                 visibility varchar(255) NOT NULL,
                                 CONSTRAINT category_pkey PRIMARY KEY (id),
                                 CONSTRAINT category_record_type_check CHECK (((record_type)::text = ANY ((ARRAY['CHECK'::character varying, 'TIME'::character varying, 'TEXT'::character varying, 'NUMBER'::character varying, 'CHECKLIST'::character varying])::text[]))),
	CONSTRAINT category_visibility_check CHECK (((visibility)::text = ANY ((ARRAY['PRIVATE'::character varying, 'PUBLIC'::character varying])::text[]))),
	CONSTRAINT fk7ffrpnxaflomhdh0qfk2jcndo FOREIGN KEY (user_id) REFERENCES public.users(id)
);


-- public.category_like definition

-- Drop table

-- DROP TABLE public.category_like;

CREATE TABLE public.category_like (
                                      created_at timestamp(6) NULL,
                                      category_id uuid NULL,
                                      id uuid NOT NULL,
                                      user_id uuid NULL,
                                      CONSTRAINT category_like_pkey PRIMARY KEY (id),
                                      CONSTRAINT uk_user_category UNIQUE (user_id, category_id),
                                      CONSTRAINT fkopmumskqicu62hep5brnjvate FOREIGN KEY (user_id) REFERENCES public.users(id),
                                      CONSTRAINT fkr3qvmfwlih4fng7dp9qpogq2u FOREIGN KEY (category_id) REFERENCES public.category(id)
);


-- public.check_list_record definition

-- Drop table

-- DROP TABLE public.check_list_record;

CREATE TABLE public.check_list_record (
                                          "date" date NULL,
                                          success bool NOT NULL,
                                          category_id uuid NULL,
                                          id uuid NOT NULL,
                                          todo varchar(255) NULL,
                                          CONSTRAINT check_list_record_pkey PRIMARY KEY (id),
                                          CONSTRAINT fk9w1h7ab82cok0m551gsw7bjr4 FOREIGN KEY (category_id) REFERENCES public.category(id)
);


-- public.check_record definition

-- Drop table

-- DROP TABLE public.check_record;

CREATE TABLE public.check_record (
                                     "date" date NULL,
                                     success bool NOT NULL,
                                     category_id uuid NULL,
                                     id uuid NOT NULL,
                                     CONSTRAINT check_record_pkey PRIMARY KEY (id),
                                     CONSTRAINT uk_category_date UNIQUE (category_id, date),
                                     CONSTRAINT fk4230o83u8pb3eoexd6n40p6q0 FOREIGN KEY (category_id) REFERENCES public.category(id)
);


-- public."comment" definition

-- Drop table

-- DROP TABLE public."comment";

CREATE TABLE public."comment" (
                                  created_at timestamp(6) NOT NULL,
                                  updated_at timestamp(6) NULL,
                                  category_id uuid NULL,
                                  id uuid NOT NULL,
                                  parent_id uuid NULL,
                                  user_id uuid NULL,
                                  "comment" varchar(255) NULL,
                                  CONSTRAINT comment_pkey PRIMARY KEY (id),
                                  CONSTRAINT fkde3rfu96lep00br5ov0mdieyt FOREIGN KEY (parent_id) REFERENCES public."comment"(id),
                                  CONSTRAINT fkqm52p1v3o13hy268he0wcngr5 FOREIGN KEY (user_id) REFERENCES public.users(id),
                                  CONSTRAINT fkr0lis29byplvvoinlypyl5opn FOREIGN KEY (category_id) REFERENCES public.category(id)
);


-- public.notification definition

-- Drop table

-- DROP TABLE public.notification;

CREATE TABLE public.notification (
                                     "read" bool NOT NULL,
                                     created_at timestamp(6) NOT NULL,
                                     id uuid NOT NULL,
                                     receiver_id uuid NOT NULL,
                                     sender_id uuid NOT NULL,
                                     "type" varchar(255) NOT NULL,
                                     "data" jsonb NULL,
                                     CONSTRAINT notification_pkey PRIMARY KEY (id),
                                     CONSTRAINT notification_type_check CHECK (((type)::text = ANY ((ARRAY['COMMENT'::character varying, 'LIKE'::character varying, 'REPLY'::character varying])::text[]))),
	CONSTRAINT fkdammjl0v5xfaegi926ugx6254 FOREIGN KEY (receiver_id) REFERENCES public.users(id),
	CONSTRAINT fkrg0atx075rr68et2rqrh34qwj FOREIGN KEY (sender_id) REFERENCES public.users(id)
);


-- public.number_record definition

-- Drop table

-- DROP TABLE public.number_record;

CREATE TABLE public.number_record (
                                      "date" date NULL,
                                      "number" float8 NULL,
                                      category_id uuid NULL,
                                      id uuid NOT NULL,
                                      CONSTRAINT number_record_pkey PRIMARY KEY (id),
                                      CONSTRAINT uk_number_record_category_date UNIQUE (category_id, date),
                                      CONSTRAINT fk235c7k7bx27qwgjf4jvn9joqx FOREIGN KEY (category_id) REFERENCES public.category(id)
);


-- public.text_record definition

-- Drop table

-- DROP TABLE public.text_record;

CREATE TABLE public.text_record (
                                    "date" date NULL,
                                    category_id uuid NOT NULL,
                                    id uuid NOT NULL,
                                    "text" text NULL,
                                    title varchar(255) NULL,
                                    CONSTRAINT text_record_pkey PRIMARY KEY (id),
                                    CONSTRAINT fk51ruhusnu538jbf5bwox04ncy FOREIGN KEY (category_id) REFERENCES public.category(id)
);


-- public.time_record definition

-- Drop table

-- DROP TABLE public.time_record;

CREATE TABLE public.time_record (
                                    "date" date NULL,
                                    "time" time(6) NULL,
                                    category_id uuid NULL,
                                    id uuid NOT NULL,
                                    CONSTRAINT time_record_pkey PRIMARY KEY (id),
                                    CONSTRAINT uk_time_record_category_date UNIQUE (category_id, date),
                                    CONSTRAINT fkpsfqxlpqa5ui8nprlosd0psdf FOREIGN KEY (category_id) REFERENCES public.category(id)
);



-- DROP FUNCTION public.armor(bytea);

CREATE OR REPLACE FUNCTION public.armor(bytea)
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_armor$function$
;

-- DROP FUNCTION public.armor(bytea, _text, _text);

CREATE OR REPLACE FUNCTION public.armor(bytea, text[], text[])
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_armor$function$
;

-- DROP FUNCTION public.crypt(text, text);

CREATE OR REPLACE FUNCTION public.crypt(text, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_crypt$function$
;

-- DROP FUNCTION public.dearmor(text);

CREATE OR REPLACE FUNCTION public.dearmor(text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_dearmor$function$
;

-- DROP FUNCTION public.decrypt(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.decrypt(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_decrypt$function$
;

-- DROP FUNCTION public.decrypt_iv(bytea, bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.decrypt_iv(bytea, bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_decrypt_iv$function$
;

-- DROP FUNCTION public.digest(bytea, text);

CREATE OR REPLACE FUNCTION public.digest(bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_digest$function$
;

-- DROP FUNCTION public.digest(text, text);

CREATE OR REPLACE FUNCTION public.digest(text, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_digest$function$
;

-- DROP FUNCTION public.encrypt(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.encrypt(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_encrypt$function$
;

-- DROP FUNCTION public.encrypt_iv(bytea, bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.encrypt_iv(bytea, bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_encrypt_iv$function$
;

-- DROP FUNCTION public.gen_random_bytes(int4);

CREATE OR REPLACE FUNCTION public.gen_random_bytes(integer)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_random_bytes$function$
;

-- DROP FUNCTION public.gen_random_uuid();

CREATE OR REPLACE FUNCTION public.gen_random_uuid()
 RETURNS uuid
 LANGUAGE c
 PARALLEL SAFE
AS '$libdir/pgcrypto', $function$pg_random_uuid$function$
;

-- DROP FUNCTION public.gen_salt(text, int4);

CREATE OR REPLACE FUNCTION public.gen_salt(text, integer)
 RETURNS text
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_gen_salt_rounds$function$
;

-- DROP FUNCTION public.gen_salt(text);

CREATE OR REPLACE FUNCTION public.gen_salt(text)
 RETURNS text
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_gen_salt$function$
;

-- DROP FUNCTION public.hmac(text, text, text);

CREATE OR REPLACE FUNCTION public.hmac(text, text, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_hmac$function$
;

-- DROP FUNCTION public.hmac(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.hmac(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pg_hmac$function$
;

-- DROP FUNCTION public.pgp_armor_headers(in text, out text, out text);

CREATE OR REPLACE FUNCTION public.pgp_armor_headers(text, OUT key text, OUT value text)
 RETURNS SETOF record
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_armor_headers$function$
;

-- DROP FUNCTION public.pgp_key_id(bytea);

CREATE OR REPLACE FUNCTION public.pgp_key_id(bytea)
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_key_id_w$function$
;

-- DROP FUNCTION public.pgp_pub_decrypt(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_text$function$
;

-- DROP FUNCTION public.pgp_pub_decrypt(bytea, bytea);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea)
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_text$function$
;

-- DROP FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_text$function$
;

-- DROP FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_bytea$function$
;

-- DROP FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_bytea$function$
;

-- DROP FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_decrypt_bytea$function$
;

-- DROP FUNCTION public.pgp_pub_encrypt(text, bytea);

CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt(text, bytea)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_encrypt_text$function$
;

-- DROP FUNCTION public.pgp_pub_encrypt(text, bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt(text, bytea, text)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_encrypt_text$function$
;

-- DROP FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_encrypt_bytea$function$
;

-- DROP FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea);

CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_pub_encrypt_bytea$function$
;

-- DROP FUNCTION public.pgp_sym_decrypt(bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt(bytea, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_decrypt_text$function$
;

-- DROP FUNCTION public.pgp_sym_decrypt(bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt(bytea, text, text)
 RETURNS text
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_decrypt_text$function$
;

-- DROP FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_decrypt_bytea$function$
;

-- DROP FUNCTION public.pgp_sym_decrypt_bytea(bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt_bytea(bytea, text)
 RETURNS bytea
 LANGUAGE c
 IMMUTABLE PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_decrypt_bytea$function$
;

-- DROP FUNCTION public.pgp_sym_encrypt(text, text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt(text, text, text)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_encrypt_text$function$
;

-- DROP FUNCTION public.pgp_sym_encrypt(text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt(text, text)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_encrypt_text$function$
;

-- DROP FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_encrypt_bytea$function$
;

-- DROP FUNCTION public.pgp_sym_encrypt_bytea(bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt_bytea(bytea, text)
 RETURNS bytea
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pgcrypto', $function$pgp_sym_encrypt_bytea$function$
;