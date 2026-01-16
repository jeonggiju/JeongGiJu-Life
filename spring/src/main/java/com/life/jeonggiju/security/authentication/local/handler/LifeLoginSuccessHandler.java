package com.life.jeonggiju.security.authentication.local.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.life.jeonggiju.security.authentication.jwt.dto.JwtDto;
import com.life.jeonggiju.security.authentication.jwt.dto.JwtInformation;
import com.life.jeonggiju.security.authentication.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.authentication.jwt.registry.JwtRegistry;
import com.life.jeonggiju.security.authentication.local.exception.UnexpectedPrincipalException;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LifeLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final ObjectMapper objectMapper;
	private final JwtRegistry jwtRegistry;
	private final JwtTokenProvider tokenProvider;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		if (!(authentication.getPrincipal() instanceof LifeUserDetails userDetails)) {
			throw new UnexpectedPrincipalException();
		}

		String accessToken = tokenProvider.generateAccessToken(userDetails);
		String refreshToken = tokenProvider.generateRefreshToken(userDetails);
		JwtDto jwtDto = new JwtDto(userDetails.getPrincipal(), accessToken);
		JwtInformation info = new JwtInformation(userDetails.getPrincipal(), accessToken, refreshToken);
		jwtRegistry.registerJwtInformation(info);

		Cookie refreshCookie = tokenProvider.generateRefreshTokenCookie(refreshToken);
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.addCookie(refreshCookie);
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
	}
}
