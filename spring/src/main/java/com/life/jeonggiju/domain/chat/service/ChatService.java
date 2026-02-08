package com.life.jeonggiju.domain.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.chat.dto.ChatMessageResponse;
import com.life.jeonggiju.domain.chat.dto.ChatRoomResponse;
import com.life.jeonggiju.domain.chat.dto.UnreadCountResponse;
import com.life.jeonggiju.domain.chat.entity.ChatMessage;
import com.life.jeonggiju.domain.chat.entity.ChatRoom;
import com.life.jeonggiju.domain.chat.exception.ChatEmptyMessageException;
import com.life.jeonggiju.domain.chat.exception.ChatNotFriendsException;
import com.life.jeonggiju.domain.chat.exception.ChatRoomForbiddenException;
import com.life.jeonggiju.domain.chat.exception.ChatRoomNotFoundException;
import com.life.jeonggiju.domain.chat.repository.ChatMessageRepository;
import com.life.jeonggiju.domain.chat.repository.ChatRoomRepository;
import com.life.jeonggiju.domain.friendship.entity.FriendshipStatus;
import com.life.jeonggiju.domain.friendship.repository.FriendshipRepository;
import com.life.jeonggiju.domain.notification.dto.NotificationCreatedDto;
import com.life.jeonggiju.domain.notification.entity.NotificationType;
import com.life.jeonggiju.domain.notification.service.NotificationService;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserRepository userRepository;
	private final FriendshipRepository friendshipRepository;
	private final NotificationService notificationService;

	@Transactional
	public ChatRoomResponse getOrCreateChatRoom(UUID userId, UUID friendId) {
		boolean areFriends = friendshipRepository.findByUsersAndStatus(userId, friendId, FriendshipStatus.ACCEPTED)
			.isPresent();
		if (!areFriends) {
			throw new ChatNotFriendsException();
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> UserNotFoundException.withUserId(userId));
		User friend = userRepository.findById(friendId)
			.orElseThrow(() -> UserNotFoundException.withUserId(friendId));

		Optional<ChatRoom> existing = chatRoomRepository.findByUsers(userId, friendId);
		ChatRoom chatRoom = existing.orElseGet(() -> chatRoomRepository.save(ChatRoom.of(user, friend)));

		int unreadCount = chatMessageRepository.countUnreadMessages(chatRoom.getId(), userId);

		return ChatRoomResponse.builder()
			.chatRoomId(chatRoom.getId())
			.friendId(friend.getId())
			.friendNickname(friend.getNickname())
			.friendEmail(friend.getEmail())
			.friendProfileImageUrl(friend.getProfileImageUrl())
			.lastMessageAt(chatRoom.getLastMessageAt())
			.unreadCount(unreadCount)
			.build();
	}

	@Transactional(readOnly = true)
	public List<ChatRoomResponse> getChatRooms(UUID userId) {
		List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(userId);
		List<ChatRoomResponse> result = new ArrayList<>();

		for (ChatRoom chatRoom : chatRooms) {
			User friend = chatRoom.getUser1().getId().equals(userId)
				? chatRoom.getUser2()
				: chatRoom.getUser1();

			int unreadCount = chatMessageRepository.countUnreadMessages(chatRoom.getId(), userId);

			List<ChatMessage> lastMessages = chatMessageRepository
				.findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), PageRequest.of(0, 1));
			String lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0).getContent();

			result.add(ChatRoomResponse.builder()
				.chatRoomId(chatRoom.getId())
				.friendId(friend.getId())
				.friendNickname(friend.getNickname())
				.friendEmail(friend.getEmail())
				.friendProfileImageUrl(friend.getProfileImageUrl())
				.lastMessage(lastMessage)
				.lastMessageAt(chatRoom.getLastMessageAt())
				.unreadCount(unreadCount)
				.build());
		}

		return result;
	}

	@Transactional
	public ChatMessageResponse sendMessage(UUID chatRoomId, UUID senderId, String content) {
		if (content == null || content.isBlank()) {
			throw new ChatEmptyMessageException();
		}

		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> ChatRoomNotFoundException.withId(chatRoomId));

		User sender = userRepository.findById(senderId)
			.orElseThrow(() -> UserNotFoundException.withUserId(senderId));

		if (!chatRoom.containsUser(sender)) {
			throw new ChatRoomForbiddenException();
		}

		ChatMessage message = ChatMessage.of(chatRoom, sender, content);
		chatMessageRepository.save(message);

		chatRoom.updateLastMessageAt();

		User receiver = chatRoom.getOtherUser(sender);
		NotificationCreatedDto dto = NotificationCreatedDto.builder()
			.receiverId(receiver.getId())
			.senderId(senderId)
			.data(Map.of(
				"chatRoomId", chatRoom.getId().toString(),
				"senderNickname", sender.getNickname(),
				"messagePreview", content.length() > 50 ? content.substring(0, 50) + "..." : content
			))
			.type(NotificationType.CHAT_MESSAGE)
			.build();
		notificationService.notify(dto);

		return ChatMessageResponse.builder()
			.messageId(message.getId())
			.senderId(sender.getId())
			.senderNickname(sender.getNickname())
			.content(message.getContent())
			.isRead(message.isRead())
			.createdAt(message.getCreatedAt())
			.build();
	}

	@Transactional(readOnly = true)
	public List<ChatMessageResponse> getMessages(UUID chatRoomId, UUID userId, int page, int size) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> ChatRoomNotFoundException.withId(chatRoomId));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> UserNotFoundException.withUserId(userId));

		if (!chatRoom.containsUser(user)) {
			throw new ChatRoomForbiddenException();
		}

		List<ChatMessage> messages = chatMessageRepository
			.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, PageRequest.of(page, size));

		List<ChatMessageResponse> result = new ArrayList<>();
		for (ChatMessage message : messages) {
			result.add(ChatMessageResponse.builder()
				.messageId(message.getId())
				.senderId(message.getSender().getId())
				.senderNickname(message.getSender().getNickname())
				.content(message.getContent())
				.isRead(message.isRead())
				.createdAt(message.getCreatedAt())
				.build());
		}

		return result;
	}

	@Transactional
	public void markAsRead(UUID chatRoomId, UUID userId) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> ChatRoomNotFoundException.withId(chatRoomId));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> UserNotFoundException.withUserId(userId));

		if (!chatRoom.containsUser(user)) {
			throw new ChatRoomForbiddenException();
		}

		chatMessageRepository.markAsReadByChatRoomIdAndUserId(chatRoomId, userId);
	}

	@Transactional(readOnly = true)
	public UnreadCountResponse getTotalUnreadCount(UUID userId) {
		int count = chatMessageRepository.countTotalUnreadMessages(userId);
		return UnreadCountResponse.builder().count(count).build();
	}
}
