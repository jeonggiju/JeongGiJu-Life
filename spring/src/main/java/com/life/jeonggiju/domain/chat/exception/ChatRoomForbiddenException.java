package com.life.jeonggiju.domain.chat.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class ChatRoomForbiddenException extends ChatException {
	public ChatRoomForbiddenException() {
		super(ErrorCode.CHAT_ROOM_FORBIDDEN);
	}
}
