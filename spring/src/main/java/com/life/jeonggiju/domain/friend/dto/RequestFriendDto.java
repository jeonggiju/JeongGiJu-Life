package com.life.jeonggiju.domain.friend.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestFriendDto {

	private UUID toUserId;
}
