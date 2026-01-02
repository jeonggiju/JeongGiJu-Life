package com.life.jeonggiju.domain.friend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.friend.dto.FriendInfo;
import com.life.jeonggiju.domain.friend.dto.IncomingByStatusResponse;
import com.life.jeonggiju.domain.friend.dto.OutgoingByStatusResponse;
import com.life.jeonggiju.domain.friend.entity.Friend;
import com.life.jeonggiju.domain.friend.entity.FriendStatus;
import com.life.jeonggiju.domain.friend.repository.FriendRepository;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {

	private final UserRepository userRepository;
	private final FriendRepository friendRepository;

	@Transactional(readOnly = true)
	public List<FriendInfo> findFriends(UUID userId) {
		List<Friend> allFriend = friendRepository.findAllAcceptedOf(userId);

		List<FriendInfo> result = new ArrayList<>();
		for (Friend friend : allFriend) {

			User other = friend.getRequester().getId().equals(userId)
				? friend.getAddressee()
				: friend.getRequester();

			FriendInfo info = FriendInfo.builder()
				.friendId(friend.getId())
				.userId(other.getId())
				.email(other.getEmail())
				.username(other.getUsername())
				.build();

			result.add(info);
		}
		return result;
	}

	@Transactional
	public void requestFriend(UUID fromId, UUID toId){

		if (fromId.equals(toId)) {
			throw new IllegalArgumentException("자기 자신에게 친구 요청할 수 없습니다.");
		}

		if (friendRepository.existsAcceptedBetween(fromId, toId)) {
			throw new IllegalStateException("이미 친구입니다.");
		}

		boolean alreadyPending = friendRepository.existsByRequester_IdAndAddressee_IdAndStatusIn(fromId, toId, List.of(FriendStatus.PENDING));
		if(alreadyPending){
			throw new IllegalStateException("이미 친구 요청을 보냈습니다.");
		}

		friendRepository.findByRequester_IdAndAddressee_IdAndStatus(toId, fromId, FriendStatus.PENDING)
			.ifPresentOrElse(
				friend -> friend.changeStatus(FriendStatus.ACCEPTED),
				() -> {
					User fromUser = userRepository.findById(fromId).orElseThrow();
					User toUser = userRepository.findById(toId).orElseThrow();

					Friend friend = Friend.builder()
						.requester(fromUser)
						.addressee(toUser)
						.status(FriendStatus.PENDING)
						.build();
					friendRepository.save(friend);
				}
			);
	}


	@Transactional(readOnly = true)
	public List<IncomingByStatusResponse> getAllInComingByStatus(UUID addresseeId, FriendStatus status){
		List<Friend> list = friendRepository.findByAddressee_IdAndStatus(addresseeId, status);

		List<IncomingByStatusResponse> result = new ArrayList<>();
		for (Friend friend : list) {
			result.add(IncomingByStatusResponse.builder()
				.email(friend.getRequester().getEmail())
				.username(friend.getRequester().getUsername())
				.createdAt(friend.getCreatedAt())
				.build());
		}
		return result;
	}

	@Transactional(readOnly = true)
	public List<OutgoingByStatusResponse> getAllOutgoingByStatus(UUID requesterId, FriendStatus status) {
		List<Friend> list = friendRepository.findByRequester_IdAndStatus(requesterId, status);

		List<OutgoingByStatusResponse> result = new ArrayList<>();
		for (Friend friend : list) {
			result.add(OutgoingByStatusResponse.builder()
				.email(friend.getAddressee().getEmail())
				.username(friend.getAddressee().getUsername())
				.createdAt(friend.getCreatedAt())
				.build());
		}
		return result;
	}

	@Transactional
	public void acceptFriend(UUID friendId, UUID accepterId){
		Friend pendingFriend = friendRepository.findByIdAndAddressee_IdAndStatus(friendId, accepterId,
			FriendStatus.PENDING);

		pendingFriend.changeStatus(FriendStatus.ACCEPTED);
	}

	@Transactional
	public void reject(UUID friendId, UUID accepterId){
		Friend pendingFriend = friendRepository.findByIdAndAddressee_IdAndStatus(friendId, accepterId,
			FriendStatus.PENDING);

		pendingFriend.changeStatus(FriendStatus.REJECTED);
	}

	@Transactional
	public void removeRequest(UUID id, UUID requesterId){
		Friend friend = friendRepository.findById(id).orElseThrow();
		if(!friend.getRequester().getId().equals(requesterId)){
			throw new IllegalArgumentException("잘못된 요청입니다.");
		}

		friendRepository.deleteById(id);
	}

	@Transactional
	public void deleteFriendByStatus(UUID requesterId, UUID accepterId, FriendStatus status){
		Friend friend = friendRepository.findBetweenByStatus(requesterId, accepterId, status).orElseThrow();
		if(friend.getStatus() != status){
			throw new IllegalArgumentException("관계가 적절하지 않습니다.");
		}
		friendRepository.delete(friend);
	}

}
