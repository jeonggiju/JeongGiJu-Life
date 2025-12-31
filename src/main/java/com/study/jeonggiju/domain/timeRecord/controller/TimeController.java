package com.study.jeonggiju.domain.timeRecord.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.timeRecord.dto.SaveTime;
import com.study.jeonggiju.domain.timeRecord.dto.UpdateTime;
import com.study.jeonggiju.domain.timeRecord.service.TimeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/time")
@RequiredArgsConstructor
public class TimeController {

	private final TimeService timeService;

	@GetMapping
	public ResponseEntity<?> find(UUID timeId){
		return ResponseEntity.ok(timeService.find(timeId));
	}

	@GetMapping("/all")
	public ResponseEntity<?> findAll(UUID categoryId){
		return ResponseEntity.ok(timeService.findAll(categoryId));
	}

	@GetMapping("/date")
	public ResponseEntity<?> findByDate(UUID categoryId, LocalDate date){
		return ResponseEntity.ok(timeService.findByDate(categoryId, date));
	}
	@PostMapping
	public ResponseEntity<?> save(
		SaveTime dto
	){
		timeService.save(dto);
		return ResponseEntity.ok().build();
	}

	@PutMapping
	public ResponseEntity<?> update(
		UpdateTime dto
	){
		timeService.update(dto);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<?> delete(UUID id){
		timeService.delete(id);
		return ResponseEntity.ok().build();
	}

}
