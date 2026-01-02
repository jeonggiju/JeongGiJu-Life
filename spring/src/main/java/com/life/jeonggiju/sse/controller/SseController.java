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

import com.life.jeonggiju.security.principal.LifeUserDetails;
import com.life.jeonggiju.sse.service.SseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

	private final SseService sseService;

	@GetMapping(value ="/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
	){
		UUID userId = userDetails.getId();
		return sseService.connect(userId, lastEventId);
	}
}
