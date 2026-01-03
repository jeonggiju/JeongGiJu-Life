package com.life.jeonggiju.domain.friend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OutgoingByStatusResponse {
	private UUID id;
	private String email;
	private String username;
	private LocalDateTime createdAt;
}
