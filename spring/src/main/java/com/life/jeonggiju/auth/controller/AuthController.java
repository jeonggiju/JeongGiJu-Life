package com.life.jeonggiju.auth.controller;

import com.life.jeonggiju.auth.dto.RefreshResponse;
import com.life.jeonggiju.auth.service.AuthService;
import com.life.jeonggiju.domain.user.dto.LoginRequest;
import com.life.jeonggiju.security.jwt.provider.JwtTokenProvider;
import com.life.jeonggiju.security.jwt.registry.JwtRegistryInformation;
import com.life.jeonggiju.security.principal.LifeUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	@Operation(
			summary = "로그인",
			description = "JSON 기반 로그인 (Spring Security Filter에서 처리됨)"
	)
	@PostMapping("/login")
	public void login(@RequestBody LoginRequest request) {
		throw new IllegalStateException("Handled by Security Filter");
	}

	@Operation(
			summary = "로그아웃",
			description = "JWT 기반 로그아웃 (Refresh Token 무효화)"
	)
	@PostMapping("/logout")
	public void logout() {
		throw new IllegalStateException("Handled by Security Filter");
	}

	@GetMapping("/csrf-token")
	public ResponseEntity<Void> csrfToken(CsrfToken token){
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponse> refreshToken(
			@CookieValue(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
			HttpServletResponse response
	){
		if(refreshToken == null){
			log.info("refreshToken is null");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

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
