package com.life.jeonggiju.domain.numberRecord.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.numberRecord.dto.FindNumberAllResponse;
import com.life.jeonggiju.domain.numberRecord.dto.FindNumberResponse;
import com.life.jeonggiju.domain.numberRecord.dto.SaveNumber;
import com.life.jeonggiju.domain.numberRecord.dto.UpdateNumber;
import com.life.jeonggiju.domain.numberRecord.service.NumberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/number")
@RequiredArgsConstructor
public class NumberController {

	private final NumberService numberService;

	@GetMapping
	public ResponseEntity<FindNumberResponse> find(UUID numberId) {
		return ResponseEntity.ok(numberService.find(numberId));
	}

	@GetMapping("/all")
	public ResponseEntity<FindNumberAllResponse> findAll(UUID categoryId) {
		return ResponseEntity.ok(numberService.findAll(categoryId));
	}

	@GetMapping("/date")
	public ResponseEntity<FindNumberResponse> findByDate(UUID categoryId, LocalDate date) {
		return ResponseEntity.ok(numberService.findByDate(categoryId, date));
	}

	@PostMapping
	public ResponseEntity<Void> save(
		SaveNumber dto
	) {
		numberService.save(dto);
		return ResponseEntity.ok().build();
	}

	@PutMapping
	public ResponseEntity<Void> update(
		UpdateNumber dto
	) {
		numberService.update(dto);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(UUID id) {
		numberService.delete(id);
		return ResponseEntity.ok().build();
	}

}
