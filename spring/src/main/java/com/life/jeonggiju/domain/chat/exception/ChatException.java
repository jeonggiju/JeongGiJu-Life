package com.life.jeonggiju.domain.chat.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class ChatException extends BaseException {
	public ChatException(ErrorCode code) {
		super(code);
	}
}
