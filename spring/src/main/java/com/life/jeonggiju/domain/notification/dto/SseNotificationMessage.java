package com.life.jeonggiju.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "SseNotificationMessage", description = "SSE notification 이벤트 data 구조")
public class SseNotificationMessage {

	@Schema(description = "클라이언트 표시용 제목/구분값", example = "notification")
	private String title;

	@Schema(description = "실제 알림 payload")
	private NotificationPayload body;

	@Schema(description = "서버 전송 시각(ms epoch)", example = "1735800000000")
	private long ts;
}
