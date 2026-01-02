package com.life.jeonggiju.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.notification.dto.NotificationCreatedDto;
import com.life.jeonggiju.domain.notification.dto.UnReadNotificationCountResponse;
import com.life.jeonggiju.domain.notification.dto.UnReadNotificationResponse;
import com.life.jeonggiju.domain.notification.entity.Notification;
import com.life.jeonggiju.domain.notification.event.NotificationCreatedEvent;
import com.life.jeonggiju.domain.notification.repository.NotificationRepository;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final ApplicationEventPublisher publisher;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<UnReadNotificationResponse> getUnRead(UUID userId){
		List<Notification> unReadNotifications = notificationRepository.findUnreadByReceiverId(userId);
		List<UnReadNotificationResponse> result = new ArrayList<>();
		for(Notification notification : unReadNotifications) {
			result.add(UnReadNotificationResponse.builder()
				.id(notification.getId())
				.isRead(notification.isRead())
				.createdAt(notification.getCreatedAt())
				.senderEmail(notification.getSender().getEmail())
				.type(notification.getType())
				.data(notification.getData()).build());
		}
		return result;
	}

	@Transactional
	public void notify(NotificationCreatedDto dto){
		User receiver = userRepository.findById(dto.getReceiverId()).orElseThrow();
		User sender = userRepository.findById(dto.getSenderId()).orElseThrow();

		Notification notification = Notification.builder()
			.receiver(receiver)
			.sender(sender)
			.data(dto.getData())
			.type(dto.getType())
			.read(false)
			.build();

		Notification save = notificationRepository.save(notification);

		NotificationCreatedEvent event = NotificationCreatedEvent.builder()
			.id(save.getId())
			.receiverId(dto.getReceiverId())
			.senderId(dto.getSenderId())
			.data(dto.getData())
			.type(dto.getType()).build();
		publisher.publishEvent(event);
	}

	@Transactional(readOnly = true)
	public UnReadNotificationCountResponse countUnRead(UUID userId){
		int i = notificationRepository.countUnreadByReceiverId(userId);
		return UnReadNotificationCountResponse.builder().count(i).build();
	}

	@Transactional
	public int setRead(List<UUID> notificationIds, UUID receiverId) {
		return notificationRepository.markAsRead(notificationIds, receiverId);
	}
}
