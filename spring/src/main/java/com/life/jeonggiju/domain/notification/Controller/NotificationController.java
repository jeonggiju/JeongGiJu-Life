package com.life.jeonggiju.domain.notification.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.notification.dto.MarkNotificationDto;
import com.life.jeonggiju.domain.notification.dto.UnReadNotificationCountResponse;
import com.life.jeonggiju.domain.notification.dto.UnReadNotificationResponse;
import com.life.jeonggiju.domain.notification.service.NotificationService;
import com.life.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notification/unread")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public List<UnReadNotificationResponse> getUnRead(
		@AuthenticationPrincipal LifeUserDetails userDetails
	){
		return notificationService.getUnRead(userDetails.getId());
	}

	@GetMapping("/count")
	UnReadNotificationCountResponse getUnReadCount(@AuthenticationPrincipal LifeUserDetails userDetails){
		return notificationService.countUnRead(userDetails.getId());
	}

	@PostMapping
	public int markAsRead(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody MarkNotificationDto dto
	){
		return notificationService.setRead(dto.getNotificationIds(), userDetails.getId());
	}
}
