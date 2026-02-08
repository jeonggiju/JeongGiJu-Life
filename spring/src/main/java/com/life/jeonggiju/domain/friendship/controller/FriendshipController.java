package com.life.jeonggiju.domain.friendship.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.friendship.dto.FriendshipRequest;
import com.life.jeonggiju.domain.friendship.dto.FriendshipResponse;
import com.life.jeonggiju.domain.friendship.dto.PendingFriendshipResponse;
import com.life.jeonggiju.domain.friendship.dto.UserSearchResponse;
import com.life.jeonggiju.domain.friendship.service.FriendshipService;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

	private final FriendshipService friendshipService;

	@PostMapping("/request")
	public ResponseEntity<Void> sendRequest(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody FriendshipRequest request
	) {
		friendshipService.sendRequest(userDetails.getId(), request.getReceiverId());
		return ResponseEntity.ok().build();
	}

	@PostMapping("/accept/{friendshipId}")
	public ResponseEntity<Void> acceptRequest(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@PathVariable UUID friendshipId
	) {
		friendshipService.acceptRequest(friendshipId, userDetails.getId());
		return ResponseEntity.ok().build();
	}

	@PostMapping("/reject/{friendshipId}")
	public ResponseEntity<Void> rejectRequest(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@PathVariable UUID friendshipId
	) {
		friendshipService.rejectRequest(friendshipId, userDetails.getId());
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<List<FriendshipResponse>> getFriends(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		return ResponseEntity.ok(friendshipService.getFriends(userDetails.getId()));
	}

	@GetMapping("/pending")
	public ResponseEntity<List<PendingFriendshipResponse>> getPendingRequests(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		return ResponseEntity.ok(friendshipService.getPendingRequests(userDetails.getId()));
	}

	@DeleteMapping("/{friendshipId}")
	public ResponseEntity<Void> deleteFriendship(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@PathVariable UUID friendshipId
	) {
		friendshipService.deleteFriendship(friendshipId, userDetails.getId());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/search")
	public ResponseEntity<List<UserSearchResponse>> searchUsers(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestParam String q
	) {
		return ResponseEntity.ok(friendshipService.searchUsers(q, userDetails.getId()));
	}
}
