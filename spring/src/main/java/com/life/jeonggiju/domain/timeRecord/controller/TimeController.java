package com.life.jeonggiju.domain.timeRecord.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.life.jeonggiju.domain.timeRecord.dto.FindTimeAllResponse;
import com.life.jeonggiju.domain.timeRecord.dto.FindTimeResponse;
import com.life.jeonggiju.domain.timeRecord.dto.SaveTime;
import com.life.jeonggiju.domain.timeRecord.dto.UpdateTime;
import com.life.jeonggiju.domain.timeRecord.service.TimeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/time")
@RequiredArgsConstructor
public class TimeController {

	private final TimeService timeService;

	@GetMapping
	public ResponseEntity<FindTimeResponse> find(UUID timeId) {
		return ResponseEntity.ok(timeService.find(timeId));
	}

	@GetMapping("/all")
	public ResponseEntity<FindTimeAllResponse> findAll(UUID categoryId) {
		return ResponseEntity.ok(timeService.findAll(categoryId));
	}

	@GetMapping("/date")
	public ResponseEntity<FindTimeResponse> findByDate(UUID categoryId, LocalDate date) {
		return ResponseEntity.ok(timeService.findByDate(categoryId, date));
	}

	@PostMapping
	public ResponseEntity<Void> save(
		SaveTime dto
	) {
		timeService.save(dto);
		return ResponseEntity.ok().build();
	}

	@PutMapping
	public ResponseEntity<Void> update(
		UpdateTime dto
	) {
		timeService.update(dto);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(UUID id) {
		timeService.delete(id);
		return ResponseEntity.ok().build();
	}

}
