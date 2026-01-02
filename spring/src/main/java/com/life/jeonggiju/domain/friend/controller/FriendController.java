package com.life.jeonggiju.domain.friend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.friend.dto.IncomingByStatusResponse;
import com.life.jeonggiju.domain.friend.dto.OutgoingByStatusResponse;
import com.life.jeonggiju.domain.friend.dto.RequestFriendDto;
import com.life.jeonggiju.domain.friend.entity.FriendStatus;
import com.life.jeonggiju.domain.friend.service.FriendService;
import com.life.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

	private final FriendService friendService;

	@PostMapping("/requests")
	public ResponseEntity<Void> request(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody RequestFriendDto dto
	){
		friendService.requestFriend(userDetails.getId(), dto.getToUserId());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/requests/incoming")
	public ResponseEntity<List<IncomingByStatusResponse>> getIncomingRequests(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestParam FriendStatus status
	){
		List<IncomingByStatusResponse> result = friendService.getAllInComingByStatus(userDetails.getId(), status);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/requests/outgoing")
	public ResponseEntity<List<OutgoingByStatusResponse>> getOutgoingRequests(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestParam FriendStatus status
	){
		List<OutgoingByStatusResponse> result =
			friendService.getAllOutgoingByStatus(userDetails.getId(), status);
		return ResponseEntity.ok(result);
	}

}
