package com.life.jeonggiju.domain.textRecord.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.life.jeonggiju.domain.textRecord.dto.FindTextAllResponse;
import com.life.jeonggiju.domain.textRecord.dto.FindTextResponse;
import com.life.jeonggiju.domain.textRecord.dto.SaveText;
import com.life.jeonggiju.domain.textRecord.dto.UpdateText;
import com.life.jeonggiju.domain.textRecord.service.TextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/text")
@RequiredArgsConstructor
public class TextController {

	private final TextService textService;

	@GetMapping
	public ResponseEntity<FindTextResponse> find(
		UUID textId
	) {
		return ResponseEntity.ok(textService.find(textId));
	}

	@GetMapping("/date")
	public ResponseEntity<List<FindTextResponse>> findByDate(
		UUID categoryId,
		LocalDate date
	) {
		List<FindTextResponse> byDate = textService.findByDate(categoryId, date);
		return ResponseEntity.ok(byDate);
	}

	@GetMapping("/all")
	public ResponseEntity<FindTextAllResponse> findAll(
		UUID categoryId
	) {
		return ResponseEntity.ok(textService.findAll(categoryId));
	}

	@PostMapping
	public ResponseEntity<Void> save(
		@RequestPart("data") SaveText dto,
		@RequestPart(value = "images", required = false) List<MultipartFile> images
	) {
		try {
			textService.save(dto, images);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping
	public ResponseEntity<Void> update(
		@RequestPart("data") UpdateText dto,
		@RequestPart(value = "images", required = false) List<MultipartFile> images
	) {
		textService.update(dto, images);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(UUID id) {
		textService.delete(id);
		return ResponseEntity.ok().build();
	}
}
