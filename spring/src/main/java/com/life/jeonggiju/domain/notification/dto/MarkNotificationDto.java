package com.life.jeonggiju.domain.notification.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MarkNotificationDto {
	List<UUID> notificationIds;
}
