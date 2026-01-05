package com.life.jeonggiju.domain.user.controller;

import com.life.jeonggiju.domain.user.dto.SignUpRequest;
import com.life.jeonggiju.domain.user.dto.SignUpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.life.jeonggiju.domain.user.dto.SearchByEmailResponse;
import com.life.jeonggiju.domain.user.dto.UpdateUser;
import com.life.jeonggiju.domain.user.service.UserService;
import com.life.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/signup")
	public ResponseEntity<SignUpResponse> signup(@RequestBody SignUpRequest request) {
		try {
			userService.signup(request);
			return ResponseEntity.ok(new SignUpResponse(true, "회원가입 성공"));
		} catch (Exception e) {
			return ResponseEntity.badRequest()
					.body(new SignUpResponse(false, "회원가입 실패: " + e.getMessage()));
		}
	}

	@GetMapping("/search")
	public ResponseEntity<SearchByEmailResponse> searchByEmail(
		@RequestParam String email
	){
		return ResponseEntity.ok(userService.searchByEmail(email));
	}

	@GetMapping
	public ResponseEntity<?> find(
		@AuthenticationPrincipal LifeUserDetails userDetails
	){
		return ResponseEntity.ok(userService.find(userDetails.getId()));
	}

	@PutMapping
	public ResponseEntity<?> update(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		UpdateUser dto){
		userService.update(userDetails.getId(),dto);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<?> delete(
		@AuthenticationPrincipal LifeUserDetails userDetails
	){
		userService.delete(userDetails.getId());
		return ResponseEntity.ok().build();
	}
}
