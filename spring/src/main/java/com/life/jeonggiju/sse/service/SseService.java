package com.life.jeonggiju.sse.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseService {

	private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

	private static final long TIMEOUT = 60 * 60 * 1000L;

	public SseEmitter connect(UUID userId, String lastEventId){
		SseEmitter emitter = new SseEmitter(TIMEOUT);

		emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

		Runnable cleanUp = () -> remove(userId, emitter);
		emitter.onCompletion(cleanUp);
		emitter.onTimeout(cleanUp);
		emitter.onError(e-> cleanUp.run());

		sendToEmitter(emitter, "connected", Map.of("ts", System.currentTimeMillis()));

		return emitter;
	}

	public void notifyUser(UUID receiverUserId, String title, Object body){
		Map<String, Object> data = Map.of(
			"title", title,
			"body", body,
			"ts", System.currentTimeMillis()
		);

		List<SseEmitter> list = emitters.get(receiverUserId);
		if(list == null) return;

		for(SseEmitter emitter : list){
			sendToEmitter(emitter, "notification", data);
		}
	}

	private void sendToEmitter(SseEmitter emitter, String eventName, Object data) {
		try{
			emitter.send(SseEmitter
				.event()
				.name(eventName)
				.data(data)
				.id(UUID.randomUUID().toString()));
		}catch(IOException e){
			emitter.completeWithError(e);
		}
	}

	private void remove(UUID userId, SseEmitter emitter){
		CopyOnWriteArrayList<SseEmitter> usersEmitters = emitters.get(userId);
		if(usersEmitters == null)
			return;
		usersEmitters.remove(emitter);

		if(usersEmitters.isEmpty())
			emitters.remove(userId);
	}

	public void pingAll(){
		long ts = System.currentTimeMillis();
		emitters.forEach((userId, list)->{
			for(SseEmitter emitter: list){
				sendToEmitter(emitter, "ping", Map.of("ts", ts));
			}
		});
	}
}
