package com.life.jeonggiju.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.user.dto.UpdateUser;
import com.life.jeonggiju.domain.user.service.UserService;
import com.life.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

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
