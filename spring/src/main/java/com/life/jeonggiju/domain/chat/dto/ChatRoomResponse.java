package com.life.jeonggiju.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomResponse {
	private UUID chatRoomId;
	private UUID friendId;
	private String friendNickname;
	private String friendEmail;
	private String friendProfileImageUrl;
	private String lastMessage;
	private LocalDateTime lastMessageAt;
	private int unreadCount;
}
