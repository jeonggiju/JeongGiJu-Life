package com.study.jeonggiju.domain.numberRecord.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.numberRecord.dto.SaveNumber;
import com.study.jeonggiju.domain.numberRecord.dto.UpdateNumber;
import com.study.jeonggiju.domain.numberRecord.service.NumberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/number")
@RequiredArgsConstructor
public class NumberController {

	private final NumberService numberService;

	@GetMapping
	public ResponseEntity<?> find(UUID numberId){
		return ResponseEntity.ok(numberService.find(numberId));
	}

	@GetMapping("/all")
	public ResponseEntity<?> findAll(UUID categoryId){
		return ResponseEntity.ok(numberService.findAll(categoryId));
	}

	@GetMapping("/date")
	public ResponseEntity<?> findByDate(UUID categoryId, LocalDate date){
		return ResponseEntity.ok(numberService.findByDate(categoryId, date));
	}
	@PostMapping
	public ResponseEntity<?> save(
		SaveNumber dto
	){
		numberService.save(dto);
		return ResponseEntity.ok().build();
	}

	@PutMapping
	public ResponseEntity<?> update(
		UpdateNumber dto
	){
		numberService.update(dto);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<?> delete(UUID id){
		numberService.delete(id);
		return ResponseEntity.ok().build();
	}

}
