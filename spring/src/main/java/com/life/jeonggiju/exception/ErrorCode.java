package com.life.jeonggiju.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),

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
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
