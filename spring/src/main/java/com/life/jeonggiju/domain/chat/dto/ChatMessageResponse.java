package com.life.jeonggiju.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponse {
	private UUID messageId;
	private UUID senderId;
	private String senderNickname;
	private String content;
	private boolean isRead;
	private LocalDateTime createdAt;
}
