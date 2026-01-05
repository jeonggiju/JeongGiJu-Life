package com.life.jeonggiju.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.user.dto.ErrorResponse;
import com.life.jeonggiju.domain.user.dto.LoginRequest;
import com.life.jeonggiju.domain.user.dto.LoginResponse;
import com.life.jeonggiju.domain.user.dto.LogoutResponse;
import com.life.jeonggiju.domain.user.dto.MeResponse;
import com.life.jeonggiju.domain.user.dto.SignUpRequest;
import com.life.jeonggiju.domain.user.dto.SignUpResponse;
import com.life.jeonggiju.domain.user.service.UserService;
import com.life.jeonggiju.security.principal.LifeUserDetails;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserService userService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
		try {
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					request.getEmail(),
					request.getPassword()
				)
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);

			session.setAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				SecurityContextHolder.getContext()
			);

			return ResponseEntity.ok(new LoginResponse(true, "로그인 성공", authentication.getName()));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new LoginResponse(false, "아이디 또는 비밀번호가 잘못되었습니다", null));
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
		try {
			userService.signup(request);
			return ResponseEntity.ok(new SignUpResponse(true, "회원가입 성공"));
		} catch (Exception e) {
			return ResponseEntity.badRequest()
				.body(new SignUpResponse(false, "회원가입 실패: " + e.getMessage()));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpSession session) {
		session.invalidate();
		SecurityContextHolder.clearContext();
		return ResponseEntity.ok(new LogoutResponse(true, "로그아웃 성공"));
	}

	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser() {

		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponse("로그인이 필요합니다", "login Fail"));
		}

		LifeUserDetails userDetails = (LifeUserDetails) authentication.getPrincipal();

		return ResponseEntity.ok(new MeResponse(userDetails.getEmail()));
	}

}
