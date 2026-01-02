package com.life.jeonggiju.sse.controller;

import java.awt.*;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.life.jeonggiju.domain.notification.dto.NotificationPayload;
import com.life.jeonggiju.domain.notification.dto.SseNotificationMessage;
import com.life.jeonggiju.domain.notification.dto.SsePingMessage;
import com.life.jeonggiju.security.principal.LifeUserDetails;
import com.life.jeonggiju.sse.service.SseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

	private final SseService sseService;

	@Operation(
		summary = "SSE 구독",
		description = "SSE 스트림입니다. event: connected/ping/notification 형태로 내려옵니다. Swagger UI는 스트리밍 테스트는 못 하고 구조 문서화만 합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "text/event-stream",
				content = @Content(
					mediaType = "text/event-stream",
					schema = @Schema(oneOf = {
						SsePingMessage.class,
						SseNotificationMessage.class
					}),
					examples = {
						@ExampleObject(
							name = "connected",
							value = """
                        {"ts":1735800000000}
                        """
						),
						@ExampleObject(
							name = "notification",
							value = """
                        {"title":"notification","body":{"id":"...","receiverId":"...","senderId":"...","type":"LIKE","content":"...","createdAt":"2026-01-02T14:00:00"},"ts":1735800000000}
                        """
						)
					}
				)
			)
		}
	)
	@GetMapping(value ="/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
	){
		UUID userId = userDetails.getId();
		return sseService.connect(userId, lastEventId);
	}
}
