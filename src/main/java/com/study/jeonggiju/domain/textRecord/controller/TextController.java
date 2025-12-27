package com.study.jeonggiju.domain.textRecord.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.textRecord.dto.SaveText;
import com.study.jeonggiju.domain.textRecord.dto.UpdateText;
import com.study.jeonggiju.domain.textRecord.service.TextService;

import lombok.RequiredArgsConstructor;

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

	@GetMapping("/all")
	public ResponseEntity<?> findAll(
		UUID categoryId
	){
		return ResponseEntity.ok(textService.findAll(categoryId));
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
