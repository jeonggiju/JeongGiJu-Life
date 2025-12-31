package com.study.jeonggiju.domain.textRecord.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.textRecord.dto.FindTextResponse;
import com.study.jeonggiju.domain.textRecord.dto.SaveText;
import com.study.jeonggiju.domain.textRecord.dto.UpdateText;
import com.study.jeonggiju.domain.textRecord.service.TextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/text")
@RequiredArgsConstructor
public class TextController {

	private final TextService textService;

	@GetMapping
	public ResponseEntity<?> find(
		UUID textId
	){
		return ResponseEntity.ok(textService.find(textId));
	}

	@GetMapping( "/date")
	public ResponseEntity<?> findByDate(
		UUID categoryId,
		LocalDate date
	){
		List<FindTextResponse> byDate = textService.findByDate(categoryId, date);
		return ResponseEntity.ok(byDate);
	}

	@GetMapping("/all")
	public ResponseEntity<?> findAll(
		UUID categoryId
	){
		List<FindTextResponse> all = textService.findAll(categoryId);
		return ResponseEntity.ok(all);
	}

	@PostMapping
	public ResponseEntity<?> save(
		SaveText dto
	){
		try{
			textService.save(dto);
			return ResponseEntity.ok().build();
		}catch(Exception e){
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping
	public ResponseEntity<?> update(
		UpdateText dto
	){
		textService.update(dto);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<?> delete(UUID id){
		textService.delete(id);
		return ResponseEntity.ok().build();
	}

}
