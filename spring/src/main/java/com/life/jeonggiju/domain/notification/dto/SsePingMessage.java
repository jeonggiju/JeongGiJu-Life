package com.life.jeonggiju.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "SsePingMessage", description = "SSE ping/connected 이벤트 data 구조")
public class SsePingMessage {

	@Schema(description = "서버 전송 시각(ms epoch)", example = "1735800000000")
	private long ts;
}
