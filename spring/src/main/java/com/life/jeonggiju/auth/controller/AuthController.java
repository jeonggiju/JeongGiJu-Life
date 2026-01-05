package com.life.jeonggiju.auth.controller;

import com.life.jeonggiju.auth.dto.RefreshResponse;
import com.life.jeonggiju.auth.service.AuthService;
import com.life.jeonggiju.security.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.jwt.registry.JwtRegistryInformation;
import com.life.jeonggiju.security.principal.LifeUserDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import com.life.jeonggiju.exception.dto.ErrorResponse;
import com.life.jeonggiju.domain.user.dto.MeResponse;
import com.life.jeonggiju.domain.user.service.UserService;
import com.life.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	@GetMapping("/csrf-token")
	public ResponseEntity<Void> csrfToken(CsrfToken token){
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponse> refreshToken(
			@CookieValue(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
			HttpServletResponse response
	){
		JwtRegistryInformation information = authService.refreshToken(refreshToken);
		Cookie cookie = jwtTokenProvider.generateRefreshTokenCookie(information.getRefreshToken());
		response.addCookie(cookie);
		return ResponseEntity.ok(RefreshResponse.builder().userId(information.getUserId()).accessToken(information.getAccessToken()).build());
	}

	@GetMapping("/me")
	public ResponseEntity<LifeUserDto> getCurrentUser() {

		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		LifeUserDetails userDetails = (LifeUserDetails) authentication.getPrincipal();
		return ResponseEntity.status(HttpStatus.OK).body(userDetails.getUserPrincipal());
	}

}
