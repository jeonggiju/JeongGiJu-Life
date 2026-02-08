package com.life.jeonggiju.domain.friendship.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.friendship.dto.FriendshipResponse;
import com.life.jeonggiju.domain.friendship.dto.PendingFriendshipResponse;
import com.life.jeonggiju.domain.friendship.dto.UserSearchResponse;
import com.life.jeonggiju.domain.friendship.entity.Friendship;
import com.life.jeonggiju.domain.friendship.entity.FriendshipStatus;
import com.life.jeonggiju.domain.friendship.exception.DuplicateFriendshipRequestException;
import com.life.jeonggiju.domain.friendship.exception.FriendshipForbiddenException;
import com.life.jeonggiju.domain.friendship.exception.FriendshipNotFoundException;
import com.life.jeonggiju.domain.friendship.exception.FriendshipNotPendingException;
import com.life.jeonggiju.domain.friendship.exception.SelfFriendshipRequestException;
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
public class FriendshipService {

	private final FriendshipRepository friendshipRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;

	@Transactional
	public void sendRequest(UUID requesterId, UUID receiverId) {
		if (requesterId.equals(receiverId)) {
			throw new SelfFriendshipRequestException();
		}

		Optional<Friendship> existing = friendshipRepository.findByUsers(requesterId, receiverId);
		if (existing.isPresent()) {
			Friendship friendship = existing.get();
			if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
				throw new DuplicateFriendshipRequestException();
			}
			if (friendship.getStatus() == FriendshipStatus.PENDING) {
				throw new DuplicateFriendshipRequestException();
			}
		}

		User requester = userRepository.findById(requesterId)
			.orElseThrow(() -> UserNotFoundException.withUserId(requesterId));
		User receiver = userRepository.findById(receiverId)
			.orElseThrow(() -> UserNotFoundException.withUserId(receiverId));

		friendshipRepository.save(Friendship.of(requester, receiver));

		NotificationCreatedDto dto = NotificationCreatedDto.builder()
			.receiverId(receiverId)
			.senderId(requesterId)
			.data(Map.of(
				"senderNickname", requester.getNickname(),
				"senderEmail", requester.getEmail()
			))
			.type(NotificationType.FRIEND_REQUEST)
			.build();
		notificationService.notify(dto);
	}

	@Transactional
	public void acceptRequest(UUID friendshipId, UUID userId) {
		Friendship friendship = friendshipRepository.findById(friendshipId)
			.orElseThrow(() -> FriendshipNotFoundException.withId(friendshipId));

		if (!friendship.getReceiver().getId().equals(userId)) {
			throw new FriendshipForbiddenException();
		}

		if (friendship.getStatus() != FriendshipStatus.PENDING) {
			throw new FriendshipNotPendingException();
		}

		friendship.accept();
		friendshipRepository.save(friendship);

		User receiver = friendship.getReceiver();
		NotificationCreatedDto dto = NotificationCreatedDto.builder()
			.receiverId(friendship.getRequester().getId())
			.senderId(userId)
			.data(Map.of(
				"senderNickname", receiver.getNickname(),
				"senderEmail", receiver.getEmail()
			))
			.type(NotificationType.FRIEND_ACCEPT)
			.build();
		notificationService.notify(dto);
	}

	@Transactional
	public void rejectRequest(UUID friendshipId, UUID userId) {
		Friendship friendship = friendshipRepository.findById(friendshipId)
			.orElseThrow(() -> FriendshipNotFoundException.withId(friendshipId));

		if (!friendship.getReceiver().getId().equals(userId)) {
			throw new FriendshipForbiddenException();
		}

		if (friendship.getStatus() != FriendshipStatus.PENDING) {
			throw new FriendshipNotPendingException();
		}

		friendship.reject();
		friendshipRepository.save(friendship);
	}

	@Transactional(readOnly = true)
	public List<FriendshipResponse> getFriends(UUID userId) {
		List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);
		List<FriendshipResponse> result = new ArrayList<>();

		for (Friendship friendship : friendships) {
			User friend = friendship.getRequester().getId().equals(userId)
				? friendship.getReceiver()
				: friendship.getRequester();

			result.add(FriendshipResponse.builder()
				.friendshipId(friendship.getId())
				.friendId(friend.getId())
				.friendEmail(friend.getEmail())
				.friendNickname(friend.getNickname())
				.friendProfileImageUrl(friend.getProfileImageUrl())
				.status(friendship.getStatus())
				.createdAt(friendship.getCreatedAt())
				.build());
		}

		return result;
	}

	@Transactional(readOnly = true)
	public List<PendingFriendshipResponse> getPendingRequests(UUID userId) {
		List<Friendship> pending = friendshipRepository.findPendingRequestsByReceiverId(userId);
		List<PendingFriendshipResponse> result = new ArrayList<>();

		for (Friendship friendship : pending) {
			User requester = friendship.getRequester();
			result.add(PendingFriendshipResponse.builder()
				.friendshipId(friendship.getId())
				.requesterId(requester.getId())
				.requesterEmail(requester.getEmail())
				.requesterNickname(requester.getNickname())
				.requesterProfileImageUrl(requester.getProfileImageUrl())
				.createdAt(friendship.getCreatedAt())
				.build());
		}

		return result;
	}

	@Transactional
	public void deleteFriendship(UUID friendshipId, UUID userId) {
		Friendship friendship = friendshipRepository.findById(friendshipId)
			.orElseThrow(() -> FriendshipNotFoundException.withId(friendshipId));

		boolean isRequester = friendship.getRequester().getId().equals(userId);
		boolean isReceiver = friendship.getReceiver().getId().equals(userId);

		if (!isRequester && !isReceiver) {
			throw new FriendshipForbiddenException();
		}

		friendshipRepository.delete(friendship);
	}

	@Transactional(readOnly = true)
	public List<UserSearchResponse> searchUsers(String query, UUID currentUserId) {
		List<User> users = userRepository.searchByNicknameOrEmail(query);
		List<UserSearchResponse> result = new ArrayList<>();

		for (User user : users) {
			if (user.getId().equals(currentUserId)) {
				continue;
			}
			result.add(UserSearchResponse.builder()
				.userId(user.getId())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.profileImageUrl(user.getProfileImageUrl())
				.build());
		}

		return result;
	}
}
