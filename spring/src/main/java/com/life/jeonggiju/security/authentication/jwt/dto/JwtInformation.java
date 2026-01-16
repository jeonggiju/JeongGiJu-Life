package com.life.jeonggiju.security.authentication.jwt.dto;

import com.life.jeonggiju.security.core.dto.UserPrincipal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtInformation {

	private UserPrincipal principal;
	private String accessToken;
	private String refreshToken;

	public void rotate(String newAccessToken, String newRefreshToken) {
		this.accessToken = newAccessToken;
		this.refreshToken = newRefreshToken;
	}
}
