package com.life.jeonggiju.security.authentication.jwt.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class TokenGenerateException extends BaseException {
	public TokenGenerateException() {
		super(ErrorCode.TOKEN_GENERATE_FAIL);
	}
}
