package com.life.jeonggiju.domain.category.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.category.dto.AddCategory;
import com.life.jeonggiju.domain.category.dto.FindCategoryResponse;
import com.life.jeonggiju.domain.category.dto.LikeEmailCategoryResponse;
import com.life.jeonggiju.domain.category.dto.PublicCategorySortKey;
import com.life.jeonggiju.domain.category.dto.PublicCategorySummaryResponse;
import com.life.jeonggiju.domain.category.dto.SortDir;
import com.life.jeonggiju.domain.category.dto.UpdateCategory;
import com.life.jeonggiju.domain.category.service.CategoryService;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping("/all")
	public ResponseEntity<List<FindCategoryResponse>> findAll(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		return ResponseEntity.ok(
			categoryService.findAll(userDetails.getId())
		);
	}

	@GetMapping("/{categoryId}/email")
	public ResponseEntity<List<LikeEmailCategoryResponse>> getEmails(
		@PathVariable UUID categoryId
	) {
		List<LikeEmailCategoryResponse> emailLikeUser = categoryService.findEmailLikeUser(categoryId);
		return ResponseEntity.ok(emailLikeUser);
	}

	@GetMapping
	public ResponseEntity<FindCategoryResponse> find(UUID id) {
		return ResponseEntity.ok(categoryService.find(id));
	}

	@PostMapping
	public ResponseEntity<Void> add(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		AddCategory addCategory
	) {
		categoryService.save(userDetails.getId(), addCategory);
		return ResponseEntity.ok().build();
	}

	@PutMapping
	public ResponseEntity<Void> update(
		UpdateCategory updateCategory
	) {
		categoryService.update(updateCategory);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(UUID id) {
		categoryService.delete(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/public/summary")
	public ResponseEntity<PublicCategorySummaryResponse> findPublicCategories(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		@RequestParam(required = false) List<PublicCategorySortKey> key,
		@RequestParam(required = false) SortDir sort,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam(defaultValue = "20") int size
	) {
		List<PublicCategorySortKey> sortKeys = (key == null || key.isEmpty())
			? List.of(PublicCategorySortKey.createdAt)
			: key;
		SortDir sortDir = (sort == null) ? SortDir.desc : sort;

		UUID userId = (userDetails != null) ? userDetails.getId() : null;

		PublicCategorySummaryResponse response = categoryService.findPublicCategoriesSummary(
			userId,
			sortKeys,
			sortDir,
			cursor,
			idAfter,
			size
		);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/public/summary/no-token")
	public ResponseEntity<PublicCategorySummaryResponse> findPublicCategories(
		@RequestParam(required = false) List<PublicCategorySortKey> key,
		@RequestParam(required = false) SortDir sort,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) UUID idAfter,
		@RequestParam(defaultValue = "20") int size
	) {
		List<PublicCategorySortKey> sortKeys = (key == null || key.isEmpty())
			? List.of(PublicCategorySortKey.createdAt)
			: key;
		SortDir sortDir = (sort == null) ? SortDir.desc : sort;

		PublicCategorySummaryResponse response = categoryService.findPublicCategoriesSummary(
			null,
			sortKeys,
			sortDir,
			cursor,
			idAfter,
			size
		);

		return ResponseEntity.ok(response);
	}
}
