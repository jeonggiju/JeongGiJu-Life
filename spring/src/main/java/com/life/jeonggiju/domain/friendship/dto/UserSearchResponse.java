package com.life.jeonggiju.domain.friendship.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchResponse {
	private UUID userId;
	private String email;
	private String nickname;
	private String profileImageUrl;
}
