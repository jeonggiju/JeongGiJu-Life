package com.study.jeonggiju.domain.checkRecord.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.checkRecord.dto.SaveCheck;
import com.study.jeonggiju.domain.checkRecord.dto.UpdateCheck;
import com.study.jeonggiju.domain.checkRecord.service.CheckService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/check")
@RequiredArgsConstructor
public class CheckController {
	private final CheckService checkService;

	@GetMapping("/date")
	public ResponseEntity<?> findByDate(
		UUID categoryId,
		LocalDate date
	){
		return ResponseEntity.ok(checkService.findByDate(categoryId,date));
	}

	@GetMapping
	public ResponseEntity<?> find(
		UUID checkId
	){
		return ResponseEntity.ok(checkService.find(checkId));
	}

	@GetMapping("/all")
	public ResponseEntity<?> findAll(
		UUID categoryId
	){
		return ResponseEntity.ok(checkService.findAll(categoryId));
	}

	@PostMapping
	public ResponseEntity<?> save(
		SaveCheck saveCheck
	){
		try{
			checkService.save(saveCheck);
			return ResponseEntity.ok().build();
		}catch(Exception e){
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping
	public ResponseEntity<?> update(
		UpdateCheck dto
	){
		checkService.update(dto);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<?> delete(UUID id){
		checkService.delete(id);
		return ResponseEntity.ok().build();
	}
}
