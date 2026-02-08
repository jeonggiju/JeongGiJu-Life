package com.life.jeonggiju.domain.friendship.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendshipRequest {
	private UUID receiverId;
}
