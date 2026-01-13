package com.life.jeonggiju.security.authentication.jwt.dto;

import com.life.jeonggiju.security.core.dto.UserPrincipal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "JWT 응답 DTO")
public class JwtDto {

	@Schema(description = "로그인한 사용자 정보")
	private UserPrincipal principal;

	@Schema(description = "Access Token (JWT)",
		example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0In0.signature")
	private String accessToken;
}
