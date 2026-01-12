package com.life.jeonggiju.security.authentication.jwt.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class InValidAccessTokenException extends BaseException {
	public InValidAccessTokenException() {
		super(ErrorCode.INVALID_ACCESS_TOKEN);
	}
}

