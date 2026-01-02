package com.life.jeonggiju.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.life.jeonggiju.domain.notification.entity.NotificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPayload {
	UUID id;
	UUID receiverId;
	UUID senderId;
	NotificationType type;

	@Schema(description = "타입별 추가 데이터(JSON). type이 COMMENT일 경우, senderEmail, comment를 키 값으로 가짐. type이 LIKE일 경우 senderEmail를 키 값으로 가짐 ")
	Map<String, Object> data;
	LocalDateTime createdAt;
}
