package com.study.jeonggiju.domain.category.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.category.dto.FindCommentResponse;
import com.study.jeonggiju.domain.category.dto.UpdateCommentRequest;
import com.study.jeonggiju.domain.category.service.CommentService;
import com.study.jeonggiju.domain.user.dto.CreateCommentRequest;
import com.study.jeonggiju.domain.user.dto.CreateCommentResponse;
import com.study.jeonggiju.domain.user.dto.CreateReplyRequest;
import com.study.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category/comment")
public class CommentController {

	private final CommentService commentService;

	@GetMapping("/{categoryId}")
	public ResponseEntity<List<FindCommentResponse>> find(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@PathVariable UUID categoryId
	) {
		UUID userId = (userDetails == null) ? null : userDetails.getId();
		return ResponseEntity.ok(commentService.find(userId, categoryId));
	}

	@PostMapping
	public ResponseEntity<CreateCommentResponse> createComment(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody CreateCommentRequest req
	) {
		UUID commentId = commentService.createComment(req, userDetails.getId());
		return ResponseEntity.ok(CreateCommentResponse.builder().commentId(commentId).build());
	}

	@PostMapping("/replies")
	public ResponseEntity<Map<String, UUID>> createReply(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody CreateReplyRequest req
	) {
		UUID replyId = commentService.createReply(req.getCategoryId(), req.getParentId(), userDetails.getId(), req.getComment());
		return ResponseEntity.ok(Map.of("commentId", replyId));
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@PathVariable UUID commentId
	) {
		commentService.delete(commentId, userDetails.getId());
		return ResponseEntity.ok().build();
	}

	@PatchMapping
	public ResponseEntity<Void> update(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody UpdateCommentRequest dto
	) {
		commentService.update(dto, userDetails.getId());
		return ResponseEntity.ok().build();
	}
}
