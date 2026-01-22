package com.life.jeonggiju.domain.user.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class UserException extends BaseException {
	public UserException(ErrorCode code) {
		super(code);
	}
}
