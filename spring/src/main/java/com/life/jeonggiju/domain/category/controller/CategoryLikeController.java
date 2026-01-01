package com.life.jeonggiju.domain.category.controller;

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

import com.life.jeonggiju.domain.category.dto.AddLikeDto;
import com.life.jeonggiju.domain.category.dto.DeleteLikeDto;
import com.life.jeonggiju.domain.category.service.CategoryLikeService;
import com.life.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories/likes")
@RequiredArgsConstructor
public class CategoryLikeController {

	private final CategoryLikeService categoryLikeService;


	@GetMapping("/count/{categoryId}")
	public ResponseEntity<Integer> count(
		@PathVariable UUID categoryId
	){
		return ResponseEntity.ok(categoryLikeService.countByCategoryId(categoryId));
	}

	@PostMapping
	public ResponseEntity<Void> add(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody AddLikeDto dto
	){
		UUID userId = userDetails.getId();
		categoryLikeService.add(userId, dto.getCategoryId());
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestBody DeleteLikeDto dto
	){
		UUID userId = userDetails.getId();
		categoryLikeService.delete(userId, dto.getCategoryId());
		return ResponseEntity.ok().build();
	}
}
