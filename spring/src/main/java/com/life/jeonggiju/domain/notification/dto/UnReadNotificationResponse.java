package com.life.jeonggiju.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.life.jeonggiju.domain.notification.entity.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnReadNotificationResponse {
	private UUID id;
	private NotificationType type;
	private Map<String, Object> data;
	private String senderEmail;
	private boolean isRead;
	private LocalDateTime createdAt;
}
