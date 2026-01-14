package com.life.jeonggiju.sse.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.life.jeonggiju.domain.notification.dto.NotificationPayload;
import com.life.jeonggiju.domain.notification.dto.SseNotificationMessage;
import com.life.jeonggiju.domain.notification.dto.SsePingMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

	private static final long TIMEOUT = 60 * 60 * 1000L;
	private static final int MAX_EMITTERS_PER_USER = 3;
	private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public SseEmitter connect(UUID userId, String lastEventId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT);

		CopyOnWriteArrayList<SseEmitter> userEmitters =
			emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());

		while (userEmitters.size() >= MAX_EMITTERS_PER_USER) {
			SseEmitter oldEmitter = userEmitters.remove(0);
			try {
				oldEmitter.complete();
			} catch (Exception e) {
				log.debug("Failed to close old emitter (already closed): {}", e.getMessage());
			}
		}

		userEmitters.add(emitter);

		Runnable cleanUp = () -> remove(userId, emitter);
		emitter.onCompletion(cleanUp);
		emitter.onTimeout(() -> {
			cleanUp.run();
		});
		emitter.onError(e -> {
			log.warn("SSE connection error for user: {}, error: {}", userId, e.getMessage());
			cleanUp.run();
		});

		sendToEmitter(emitter, "connected", Map.of(
			"ts", System.currentTimeMillis(),
			"activeConnections", userEmitters.size()
		));

		return emitter;
	}

	public void notifyUser(UUID receiverUserId, String title, NotificationPayload body) {
		CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(receiverUserId);
		if (userEmitters == null || userEmitters.isEmpty()) {
			log.debug("No active SSE connections for user: {}", receiverUserId);
			return;
		}

		SseNotificationMessage data = SseNotificationMessage.builder()
			.title(title)
			.body(body)
			.ts(System.currentTimeMillis())
			.build();

		List<SseEmitter> deadEmitters = new ArrayList<>();

		for (SseEmitter emitter : userEmitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("notification")
					.data(data)
					.id(UUID.randomUUID().toString()));
			} catch (Exception e) {
				log.warn("Failed to send notification to user: {}, error: {}", receiverUserId, e.getMessage());
				deadEmitters.add(emitter);
			}
		}

		deadEmitters.forEach(emitter -> remove(receiverUserId, emitter));
	}

	private void sendToEmitter(SseEmitter emitter, String eventName, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.name(eventName)
				.data(data)
				.id(UUID.randomUUID().toString()));
		} catch (IOException e) {
			log.debug("Failed to send event to emitter: {}", e.getMessage());
			try {
				emitter.completeWithError(e);
			} catch (Exception ex) {
			}
		}
	}

	private void remove(UUID userId, SseEmitter emitter) {
		CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
		if (userEmitters == null) {
			return;
		}

		userEmitters.remove(emitter);

		if (userEmitters.isEmpty()) {
			emitters.remove(userId);
			log.info("All SSE connections closed for user: {}", userId);
		}
	}

	public void pingAll() {
		SsePingMessage pingMessage = SsePingMessage.builder()
			.ts(System.currentTimeMillis())
			.build();

		List<UUID> usersToClean = new ArrayList<>();

		emitters.forEach((userId, userEmitters) -> {
			List<SseEmitter> deadEmitters = new ArrayList<>();

			for (SseEmitter emitter : userEmitters) {
				try {
					emitter.send(SseEmitter.event()
						.name("ping")
						.data(pingMessage)
						.id(UUID.randomUUID().toString()));
				} catch (Exception e) {
					log.debug("Failed to ping user: {}, error: {}", userId, e.getMessage());
					deadEmitters.add(emitter);
				}
			}

			deadEmitters.forEach(emitter -> remove(userId, emitter));

			if (userEmitters.isEmpty()) {
				usersToClean.add(userId);
			}
		});

		usersToClean.forEach(emitters::remove);

		log.debug("Ping sent to all connections. Active users: {}", emitters.size());
	}

	public void disconnectUser(UUID userId) {
		CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.remove(userId);
		if (userEmitters != null) {
			for (SseEmitter emitter : userEmitters) {
				try {
					emitter.complete();
				} catch (Exception e) {
					log.debug("Failed to complete emitter for user: {}, error: {}", userId, e.getMessage());
				}
			}
		}
	}

	public int getActiveConnectionCount() {
		return emitters.values().stream()
			.mapToInt(CopyOnWriteArrayList::size)
			.sum();
	}

	public int getUserConnectionCount(UUID userId) {
		CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(userId);
		return userEmitters != null ? userEmitters.size() : 0;
	}
}