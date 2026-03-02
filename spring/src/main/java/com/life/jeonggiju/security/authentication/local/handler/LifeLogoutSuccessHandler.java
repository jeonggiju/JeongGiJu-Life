package com.life.jeonggiju.security.authentication.local.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.life.jeonggiju.security.authentication.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.authentication.jwt.registry.JwtRegistry;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LifeLogoutSuccessHandler implements LogoutSuccessHandler {

	private final JwtTokenProvider tokenProvider;
	private final JwtRegistry jwtRegistry;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof LifeUserDetails userDetails) {
			jwtRegistry.invalidateJwtInformationByUserId(userDetails.getId());
		}
		Cookie cookie = tokenProvider.generateRefreshTokenExpirationCookie();
		response.addCookie(cookie);
	}
}
