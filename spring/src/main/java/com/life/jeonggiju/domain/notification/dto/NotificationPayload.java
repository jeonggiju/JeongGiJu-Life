package com.life.jeonggiju.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.life.jeonggiju.domain.notification.entity.NotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPayload {
	UUID id;
	UUID receiverId;
	UUID senderId;
	NotificationType type;
	String content;
	LocalDateTime createdAt;
}
