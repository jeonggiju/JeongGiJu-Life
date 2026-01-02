package com.life.jeonggiju.domain.notification.event;

import java.util.Map;
import java.util.UUID;

import com.life.jeonggiju.domain.notification.entity.NotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationCreatedEvent {
	private UUID id;
	private UUID receiverId;
	private UUID senderId;
	private NotificationType type;
	private Map<String, Object> data;
}
