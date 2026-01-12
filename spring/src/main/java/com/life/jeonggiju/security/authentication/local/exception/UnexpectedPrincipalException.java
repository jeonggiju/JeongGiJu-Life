package com.life.jeonggiju.security.authentication.local.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class UnexpectedPrincipalException extends BaseException {
	public UnexpectedPrincipalException() {
		super(ErrorCode.UNEXPECTED_PRINCIPAL);
	}
}
