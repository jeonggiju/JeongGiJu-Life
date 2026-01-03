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

	@Schema(description = "타입별 추가 데이터(JSON). "
		+ "type이 REPLY일 경우, categoryTitle, myComment, senderEmail, comment를 키 값으로 가짐"
		+ "type이 COMMENT일 경우, categoryTitle ,senderEmail, comment를 키 값으로 가짐. "
		+ "type이 LIKE일 경우 categoryTitle,senderEmail를 키 값으로 가짐"
		+ "type이 FRIEND_REQUEST일 경우 requesterEmail를 키값으로 가짐 "
		+ "type이 FRIEND_ACCEPT일 경우 키가 없음"
		+ "type이 FRIEND_REJECT일 경우 키가 없음")
	Map<String, Object> data;
	LocalDateTime createdAt;
}
