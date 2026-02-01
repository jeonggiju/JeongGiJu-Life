package com.life.jeonggiju.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),

	DUPLICATE_LIKE(HttpStatus.CONFLICT, "중복 좋아요는 불가합니다."),
	DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "데이터 무결성 제약조건을 위반했습니다."),

	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
	EMPTY_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "댓글 내용은 비어있을 수 없습니다."),
	PARENT_COMMENT_MISMATCH(HttpStatus.BAD_REQUEST, "부모 댓글의 카테고리가 일치하지 않습니다."),
	COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다."),
	COMMENT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "수정 권한이 없습니다."),

	NUMBER_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "숫자 기록을 찾을 수 없습니다."),
	TEXT_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "텍스트 기록을 찾을 수 없습니다."),
	TIME_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "시간 기록을 찾을 수 없습니다."),
	CHECK_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "체크 기록을 찾을 수 없습니다."),
	CHECK_LIST_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "체크리스트 기록을 찾을 수 없습니다."),
	EXPENSE_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "가계부 기록을 찾을 수 없습니다."),
	EXPENSE_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "가계부 태그를 찾을 수 없습니다."),

	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다"),
	ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "계정이 잠겨있습니다"),
	FORBIDDEN_USER_ACCESS(HttpStatus.FORBIDDEN, "본인의 정보만 변경할 수 있습니다"),

	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
	LOCKED_USER_ACCESS(HttpStatus.UNAUTHORIZED, "잠금 계정입니다."),
	UNEXPECTED_PRINCIPAL(HttpStatus.INTERNAL_SERVER_ERROR, "인증 처리 중 알 수 없는 오류가 발생했습니다."),
	TOKEN_GENERATE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성에 실패했습니다"),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 유효하지 않습니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),

	PROFILE_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 이미지 업로드에 실패했습니다."),

	TEXT_RECORD_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
	TEXT_RECORD_IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
