package com.life.jeonggiju.security.core.dto;

import java.util.UUID;

import com.life.jeonggiju.domain.user.entity.Authority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserPrincipal {
	private UUID userId;
	private String email;
	private String username;
	private String title;
	private Authority authority;
}
