package com.life.jeonggiju.domain.user.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.life.jeonggiju.domain.user.dto.SignUpRequest;
import com.life.jeonggiju.domain.user.dto.UpdateUserRequest;
import com.life.jeonggiju.domain.user.dto.UserFindResponse;
import com.life.jeonggiju.domain.user.service.UserService;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<UserFindResponse> find(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		return ResponseEntity.ok(userService.find(userDetails.getId()));
	}

	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> update(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestPart(value="image", required = false) MultipartFile image,
		UpdateUserRequest dto
	) {
		userService.update(userDetails.getId(), dto, image);
		return ResponseEntity.ok().build();
	}


	@DeleteMapping
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		userService.delete(userDetails.getId());
		return ResponseEntity.ok().build();
	}

	@PostMapping
	public ResponseEntity<Void> signUp(
		@RequestBody @Valid SignUpRequest dto
	) {
		userService.signup(dto);
		return ResponseEntity.ok().build();
	}

}
