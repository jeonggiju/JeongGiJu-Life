package com.life.jeonggiju.domain.friendship.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.life.jeonggiju.domain.friendship.entity.FriendshipStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendshipResponse {
	private UUID friendshipId;
	private UUID friendId;
	private String friendEmail;
	private String friendNickname;
	private String friendProfileImageUrl;
	private FriendshipStatus status;
	private LocalDateTime createdAt;
}
