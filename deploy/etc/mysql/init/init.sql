-- MySQL 초기화 스크립트
-- docker-entrypoint-initdb.d에 의해 컨테이너 최초 기동 시 자동 실행

-- 성능 스키마 활성화 (기본 활성화되어 있음)
-- 느린 쿼리 로그 활성화
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
