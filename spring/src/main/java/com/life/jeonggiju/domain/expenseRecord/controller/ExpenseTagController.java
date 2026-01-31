package com.life.jeonggiju.domain.expenseRecord.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.expenseRecord.dto.FindExpenseTagResponse;
import com.life.jeonggiju.domain.expenseRecord.dto.SaveExpenseTag;
import com.life.jeonggiju.domain.expenseRecord.dto.UpdateExpenseTag;
import com.life.jeonggiju.domain.expenseRecord.service.ExpenseTagService;
import com.life.jeonggiju.security.core.principal.LifeUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/expense/tag")
@RequiredArgsConstructor
public class ExpenseTagController {

	private final ExpenseTagService expenseTagService;

	@GetMapping("/all")
	public ResponseEntity<List<FindExpenseTagResponse>> findAllByUser(
		@AuthenticationPrincipal LifeUserDetails userDetails
	) {
		return ResponseEntity.ok(expenseTagService.findAllByUser(userDetails.getId()));
	}

	@GetMapping
	public ResponseEntity<FindExpenseTagResponse> find(UUID tagId) {
		return ResponseEntity.ok(expenseTagService.find(tagId));
	}

	@PostMapping
	public ResponseEntity<Void> save(
		@AuthenticationPrincipal LifeUserDetails userDetails,
		SaveExpenseTag saveExpenseTag
	) {
		expenseTagService.save(userDetails.getId(), saveExpenseTag);
		return ResponseEntity.ok().build();
	}

	@PutMapping
	public ResponseEntity<Void> update(UpdateExpenseTag updateExpenseTag) {
		expenseTagService.update(updateExpenseTag);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(UUID id) {
		expenseTagService.delete(id);
		return ResponseEntity.ok().build();
	}
}
