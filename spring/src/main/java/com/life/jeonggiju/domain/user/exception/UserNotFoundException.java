package com.life.jeonggiju.domain.user.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class UserNotFoundException extends BaseException {

	public UserNotFoundException() {
		super(ErrorCode.USER_NOT_FOUND);
	}

	public static UserNotFoundException withUserId(UUID userId) {
		UserNotFoundException exception = new UserNotFoundException();
		exception.addDetail("userId", userId);
		return exception;
	}

	public static UserNotFoundException withEmail(String email) {
		UserNotFoundException exception = new UserNotFoundException();
		exception.addDetail("email", email);
		exception.addDetail("message", "invalid email");
		return exception;
	}
}
