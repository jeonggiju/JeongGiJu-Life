package com.life.jeonggiju.domain.expenseRecord.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.expenseRecord.dto.FindExpenseAllResponse;
import com.life.jeonggiju.domain.expenseRecord.dto.FindExpenseByDateResponse;
import com.life.jeonggiju.domain.expenseRecord.dto.FindExpenseResponse;
import com.life.jeonggiju.domain.expenseRecord.dto.SaveExpense;
import com.life.jeonggiju.domain.expenseRecord.dto.UpdateExpense;
import com.life.jeonggiju.domain.expenseRecord.service.ExpenseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/expense")
@RequiredArgsConstructor
public class ExpenseController {

	private final ExpenseService expenseService;

	@GetMapping("/date")
	public ResponseEntity<FindExpenseByDateResponse> findByDate(UUID categoryId, LocalDate date) {
		return ResponseEntity.ok(expenseService.findByDate(categoryId, date));
	}

	@GetMapping
	public ResponseEntity<FindExpenseResponse> find(UUID expenseId) {
		return ResponseEntity.ok(expenseService.find(expenseId));
	}

	@GetMapping("/all")
	public ResponseEntity<FindExpenseAllResponse> findAll(UUID categoryId) {
		return ResponseEntity.ok(expenseService.findAll(categoryId));
	}

	@PostMapping
	public ResponseEntity<Void> save(SaveExpense saveExpense) {
		expenseService.save(saveExpense);
		return ResponseEntity.ok().build();
	}

	@PutMapping
	public ResponseEntity<Void> update(UpdateExpense updateExpense) {
		expenseService.update(updateExpense);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(UUID id) {
		expenseService.delete(id);
		return ResponseEntity.ok().build();
	}
}
