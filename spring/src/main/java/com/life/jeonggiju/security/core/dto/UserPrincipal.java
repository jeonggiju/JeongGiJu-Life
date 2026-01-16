package com.life.jeonggiju.security.core.dto;

import java.util.UUID;

import com.life.jeonggiju.domain.user.entity.Authority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "인증된 사용자 Principal 정보")
public class UserPrincipal {

	@Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID userId;

	@Schema(description = "이메일", example = "test@life.com")
	private String email;

	@Schema(description = "유저네임", example = "jeonggiju")
	private String username;

	@Schema(description = "유저 타이틀", example = "BACKEND_ENGINEER")
	private String title;

	@Schema(description = "권한", example = "USER")
	private Authority authority;
}