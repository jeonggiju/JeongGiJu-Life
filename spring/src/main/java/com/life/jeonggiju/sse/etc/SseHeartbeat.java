package com.life.jeonggiju.sse.etc;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.life.jeonggiju.sse.service.SseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseHeartbeat {

	private final SseService sseService;

	@Scheduled(fixedDelay = 15000)
	public void ping(){
		sseService.pingAll();
	}
}
