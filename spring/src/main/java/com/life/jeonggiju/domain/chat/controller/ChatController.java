package com.life.jeonggiju.domain.chat.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.chat.dto.ChatMessageResponse;
import com.life.jeonggiju.domain.chat.dto.ChatRoomResponse;
import com.life.jeonggiju.domain.chat.dto.CreateChatRoomRequest;
import com.life.jeonggiju.domain.chat.dto.SendMessageRequest;
import com.life.jeonggiju.domain.chat.dto.UnreadCountResponse;
import com.life.jeonggiju.domain.chat.service.ChatService;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@PostMapping("/rooms")
	public ResponseEntity<ChatRoomResponse> getOrCreateChatRoom(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody CreateChatRoomRequest request
	) {
		return ResponseEntity.ok(chatService.getOrCreateChatRoom(userDetails.getId(), request.getFriendId()));
	}

	@GetMapping("/rooms")
	public ResponseEntity<List<ChatRoomResponse>> getChatRooms(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		return ResponseEntity.ok(chatService.getChatRooms(userDetails.getId()));
	}

	@PostMapping("/rooms/{chatRoomId}/messages")
	public ResponseEntity<ChatMessageResponse> sendMessage(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@PathVariable UUID chatRoomId,
		@RequestBody SendMessageRequest request
	) {
		return ResponseEntity.ok(chatService.sendMessage(chatRoomId, userDetails.getId(), request.getContent()));
	}

	@GetMapping("/rooms/{chatRoomId}/messages")
	public ResponseEntity<List<ChatMessageResponse>> getMessages(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@PathVariable UUID chatRoomId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "50") int size
	) {
		return ResponseEntity.ok(chatService.getMessages(chatRoomId, userDetails.getId(), page, size));
	}

	@PostMapping("/rooms/{chatRoomId}/read")
	public ResponseEntity<Void> markAsRead(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@PathVariable UUID chatRoomId
	) {
		chatService.markAsRead(chatRoomId, userDetails.getId());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/unread")
	public ResponseEntity<UnreadCountResponse> getTotalUnreadCount(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		return ResponseEntity.ok(chatService.getTotalUnreadCount(userDetails.getId()));
	}
}
