package com.life.jeonggiju.sse.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

	private static final long TIMEOUT = 60 * 60 * 1000L;
	private static final int MAX_EMITTERS_PER_USER = 3;
	private static final long CACHE_DURATION = 5 * 60 * 1000L;
	private static final int MAX_CACHE_SIZE = 100;

	private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

	private final Map<UUID, LinkedHashMap<String, SseEventCache>> eventCache = new ConcurrentHashMap<>();

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
			cleanUp.run();
		});

		if (lastEventId != null && !lastEventId.isEmpty()) {
			int resentCount = resendMissedEvents(emitter, userId, lastEventId);
			if (resentCount > 0) {
				log.info("Resent {} missed events to user: {}", resentCount, userId);
			}
		}

		String connectedEventId = UUID.randomUUID().toString();
		sendToEmitter(emitter, connectedEventId, "connected", Map.of(
			"ts", System.currentTimeMillis(),
			"activeConnections", userEmitters.size()
		));

		return emitter;
	}

	private int resendMissedEvents(SseEmitter emitter, UUID userId, String lastEventId) {
		LinkedHashMap<String, SseEventCache> userCache = eventCache.get(userId);

		if (userCache == null || userCache.isEmpty()) {
			return 0;
		}

		long now = System.currentTimeMillis();
		boolean foundLastEvent = false;
		int resentCount = 0;
		List<SseEventCache> eventsToResend = new ArrayList<>();

		for (SseEventCache cached : userCache.values()) {
			if (now - cached.getTimestamp() > CACHE_DURATION) {
				continue;
			}

			if (foundLastEvent) {
				eventsToResend.add(cached);
			}

			if (cached.getEventId().equals(lastEventId)) {
				foundLastEvent = true;
				log.debug("Found lastEventId: {} for user: {}", lastEventId, userId);
			}
		}

		for (SseEventCache cached : eventsToResend) {
			try {
				emitter.send(SseEmitter.event()
					.id(cached.getEventId())
					.name(cached.getEventName())
					.data(cached.getData()));
				resentCount++;
			} catch (IOException e) {
				log.error("Failed to resend event: {}, error: {}", cached.getEventId(), e.getMessage());
				break;
			}
		}

		if (!foundLastEvent && !userCache.isEmpty()) {
			log.warn("LastEventId {} not found in cache for user: {}", lastEventId, userId);
		}

		return resentCount;
	}

	public void notifyUser(UUID receiverUserId, String title, NotificationPayload body) {
		CopyOnWriteArrayList<SseEmitter> userEmitters = emitters.get(receiverUserId);
		if (userEmitters == null || userEmitters.isEmpty()) {
			return;
		}

		SseNotificationMessage data = SseNotificationMessage.builder()
			.title(title)
			.body(body)
			.ts(System.currentTimeMillis())
			.build();

		String eventId = UUID.randomUUID().toString();

		cacheEvent(receiverUserId, eventId, "notification", data);

		List<SseEmitter> deadEmitters = new ArrayList<>();

		for (SseEmitter emitter : userEmitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("notification")
					.data(data)
					.id(eventId));
			} catch (Exception e) {
				log.warn("Failed to send notification to user: {}, error: {}", receiverUserId, e.getMessage());
				deadEmitters.add(emitter);
			}
		}

		deadEmitters.forEach(emitter -> remove(receiverUserId, emitter));
	}

	private void cacheEvent(UUID userId, String eventId, String eventName, Object data) {
		LinkedHashMap<String, SseEventCache> userCache =
			eventCache.computeIfAbsent(userId, k -> new LinkedHashMap<>());

		userCache.put(eventId, new SseEventCache(
			eventId,
			eventName,
			data,
			System.currentTimeMillis()
		));

		if (userCache.size() > MAX_CACHE_SIZE) {
			Iterator<String> iterator = userCache.keySet().iterator();
			if (iterator.hasNext()) {
				String oldestKey = iterator.next();
				userCache.remove(oldestKey);
				log.debug("Removed oldest cache entry for user: {}", userId);
			}
		}
	}

	private void sendToEmitter(SseEmitter emitter, String eventId, String eventName, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.name(eventName)
				.data(data)
				.id(eventId));
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
		}

		try {
			emitter.complete();
		} catch (Exception e) {
			log.debug("Emitter already completed");
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

		cleanOldCaches();

		log.debug("Ping sent to all connections. Active users: {}", emitters.size());
	}

	private void cleanOldCaches() {
		long now = System.currentTimeMillis();
		int totalCleaned = 0;

		for (Map.Entry<UUID, LinkedHashMap<String, SseEventCache>> entry : eventCache.entrySet()) {
			UUID userId = entry.getKey();
			LinkedHashMap<String, SseEventCache> userCache = entry.getValue();

			int beforeSize = userCache.size();

			userCache.entrySet().removeIf(cacheEntry ->
				now - cacheEntry.getValue().getTimestamp() > CACHE_DURATION
			);

			int afterSize = userCache.size();
			int cleaned = beforeSize - afterSize;

			if (cleaned > 0) {
				totalCleaned += cleaned;
				log.debug("Cleaned {} old cache entries for user: {}", cleaned, userId);
			}

			if (userCache.isEmpty()) {
				eventCache.remove(userId);
			}
		}

		if (totalCleaned > 0) {
			log.info("Total cleaned cache entries: {}", totalCleaned);
		}
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

	@Getter
	@AllArgsConstructor
	private static class SseEventCache {
		private final String eventId;
		private final String eventName;
		private final Object data;
		private final long timestamp;
	}
}