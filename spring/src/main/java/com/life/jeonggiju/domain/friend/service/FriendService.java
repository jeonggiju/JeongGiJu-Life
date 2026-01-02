package com.life.jeonggiju.domain.friend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
				Friend::accept,
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


}
