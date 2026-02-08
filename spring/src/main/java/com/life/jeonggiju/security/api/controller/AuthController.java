package com.life.jeonggiju.security.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.security.api.dto.ResetPasswordRequest;
import com.life.jeonggiju.security.api.service.AuthService;
import com.life.jeonggiju.security.authentication.jwt.dto.JwtDto;
import com.life.jeonggiju.security.authentication.jwt.dto.JwtInformation;
import com.life.jeonggiju.security.authentication.jwt.provider.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	@GetMapping("/csrf-token")
	public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken, HttpServletResponse response) {
		response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(
		@CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken,
		HttpServletResponse response
	) {
		if (refreshToken == null || refreshToken.isBlank()) {
			log.warn("Refresh token cookie is missing");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("status", 401, "error", "Unauthorized", "message", "Refresh token is missing"));
		}

		try {
			JwtInformation jwtInformation = authService.refreshToken(refreshToken);
			Cookie refreshCookie = jwtTokenProvider.generateRefreshTokenCookie(jwtInformation.getRefreshToken());

			response.addCookie(refreshCookie);
			JwtDto body = new JwtDto(jwtInformation.getPrincipal(), jwtInformation.getAccessToken());

			return ResponseEntity.status(HttpStatus.OK).body(body);
		} catch (AuthenticationException e) {
			log.warn("Refresh token validation failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("status", 401, "error", "Unauthorized", "message", "Refresh token is invalid or expired"));
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<Void> resetPassword(
		@RequestBody ResetPasswordRequest dto
	) {
		authService.resetPassword(dto.getUsername(), dto.getEmail(), dto.getNewPassword());
		return ResponseEntity.ok().build();
	}
}
