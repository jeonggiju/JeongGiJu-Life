package com.study.jeonggiju.domain.categoryLike.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.categoryLike.dto.AddLikeDto;
import com.study.jeonggiju.domain.categoryLike.dto.DeleteLikeDto;
import com.study.jeonggiju.domain.categoryLike.service.CategoryLikeService;
import com.study.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryLikeController {

	private final CategoryLikeService categoryLikeService;


	@GetMapping("/likes/count/{categoryId}")
	public ResponseEntity<Integer> count(
		@PathVariable UUID categoryId
	){
		return ResponseEntity.ok(categoryLikeService.countByCategoryId(categoryId));
	}

	@PostMapping("/likes")
	public ResponseEntity<Void> add(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody AddLikeDto dto
	){
		UUID userId = userDetails.getId();
		categoryLikeService.add(userId, dto.getCategoryId());
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/likes")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody DeleteLikeDto dto
	){
		UUID userId = userDetails.getId();
		categoryLikeService.delete(userId, dto.getCategoryId());
		return ResponseEntity.ok().build();
	}
}
