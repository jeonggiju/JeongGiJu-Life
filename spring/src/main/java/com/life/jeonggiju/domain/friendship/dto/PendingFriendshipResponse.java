package com.life.jeonggiju.domain.friendship.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingFriendshipResponse {
	private UUID friendshipId;
	private UUID requesterId;
	private String requesterEmail;
	private String requesterNickname;
	private String requesterProfileImageUrl;
	private LocalDateTime createdAt;
}
