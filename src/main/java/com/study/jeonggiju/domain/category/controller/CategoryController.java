package com.study.jeonggiju.domain.category.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.category.dto.AddCategory;
import com.study.jeonggiju.domain.category.dto.UpdateCategory;
import com.study.jeonggiju.domain.category.service.CategoryService;
import com.study.jeonggiju.security.authentication.LifeAuthenticationProvider;
import com.study.jeonggiju.security.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping
	public ResponseEntity<?> findAll(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		return ResponseEntity.ok(
			categoryService.findAll(userDetails.getId())
		);
	}

	@PostMapping
	public ResponseEntity<?> add(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		AddCategory addCategory
	){
		categoryService.save(userDetails.getId(), addCategory);
		return ResponseEntity.ok().build();
	}

	@PutMapping
	public ResponseEntity<?> update(
		UpdateCategory updateCategory
	){
		categoryService.update(updateCategory);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<?> delete(UUID id){
		categoryService.delete(id);
		return ResponseEntity.ok().build();
	}

}
