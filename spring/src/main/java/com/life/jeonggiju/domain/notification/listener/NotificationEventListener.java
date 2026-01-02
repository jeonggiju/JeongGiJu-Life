package com.life.jeonggiju.domain.notification.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.life.jeonggiju.domain.notification.dto.NotificationPayload;
import com.life.jeonggiju.domain.notification.entity.Notification;
import com.life.jeonggiju.domain.notification.event.NotificationCreatedEvent;
import com.life.jeonggiju.domain.notification.repository.NotificationRepository;
import com.life.jeonggiju.sse.service.SseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationRepository notificationRepository;
	private final SseService sseService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void listen(NotificationCreatedEvent event){
		Notification notification = notificationRepository.findById(event.getId()).orElseThrow();

		NotificationPayload payload = NotificationPayload.builder()
			.id(notification.getId())
			.receiverId(notification.getReceiver().getId())
			.senderId(notification.getSender().getId())
			.content(notification.getContent())
			.type(notification.getType())
			.createdAt(notification.getCreatedAt())
			.build();

		sseService.notifyUser(payload.getReceiverId(), "notification", payload);
	}
}
