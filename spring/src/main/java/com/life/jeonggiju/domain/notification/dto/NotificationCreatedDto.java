package com.life.jeonggiju.domain.notification.dto;

import java.util.UUID;

import com.life.jeonggiju.domain.notification.entity.NotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationCreatedDto {
	private UUID receiverId;
	private UUID senderId;
	private NotificationType type;
	private String content;
}
