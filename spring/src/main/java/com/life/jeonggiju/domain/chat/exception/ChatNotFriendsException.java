package com.life.jeonggiju.domain.chat.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class ChatNotFriendsException extends ChatException {
	public ChatNotFriendsException() {
		super(ErrorCode.CHAT_NOT_FRIENDS);
	}
}
