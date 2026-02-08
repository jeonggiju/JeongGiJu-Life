package com.life.jeonggiju.domain.chat.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class ChatEmptyMessageException extends ChatException {
	public ChatEmptyMessageException() {
		super(ErrorCode.CHAT_EMPTY_MESSAGE);
	}
}
