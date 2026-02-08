package com.life.jeonggiju.domain.chat.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class ChatRoomNotFoundException extends ChatException {

	public ChatRoomNotFoundException() {
		super(ErrorCode.CHAT_ROOM_NOT_FOUND);
	}

	public static ChatRoomNotFoundException withId(UUID chatRoomId) {
		ChatRoomNotFoundException exception = new ChatRoomNotFoundException();
		exception.addDetail("chatRoomId", chatRoomId);
		return exception;
	}
}
