package com.life.jeonggiju.security.authentication.local.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class InvalidCredentialsException extends BaseException {
	public InvalidCredentialsException() {
		super(ErrorCode.INVALID_CREDENTIALS);
	}

	public static InvalidCredentialsException withEmail(UUID email) {
		InvalidCredentialsException exception = new InvalidCredentialsException();
		exception.addDetail("email", email);
		return exception;
	}

	public static InvalidCredentialsException withPassword() {
		return new InvalidCredentialsException();
	}
}
