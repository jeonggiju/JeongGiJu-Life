package com.life.jeonggiju.domain.friend.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendInfo {
	private UUID friendId;
	private UUID userId;
	private String email;
	private String username;
}
