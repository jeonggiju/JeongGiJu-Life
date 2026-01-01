package com.study.jeonggiju.domain.checkList.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.checkList.dto.SaveCheckList;
import com.study.jeonggiju.domain.checkList.dto.UpdateCheckList;
import com.study.jeonggiju.domain.checkList.service.CheckListService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/check-list")
@RequiredArgsConstructor
public class CheckListController {
	private final CheckListService checkListService;

	@GetMapping("/date")
	public ResponseEntity<?> findByDate(
		UUID categoryId,
		LocalDate date
	){
		return ResponseEntity.ok(checkListService.findByDate(categoryId,date));
	}

	@GetMapping
	public ResponseEntity<?> find(
		UUID checkId
	){
		return ResponseEntity.ok(checkListService.find(checkId));
	}

	@GetMapping("/all")
	public ResponseEntity<?> findAll(
		UUID categoryId
	){
		return ResponseEntity.ok(checkListService.findAll(categoryId));
	}

	@PostMapping
	public ResponseEntity<?> save(
		SaveCheckList saveCheckList
	){
		try{
			checkListService.save(saveCheckList);
			return ResponseEntity.ok().build();
		}catch(Exception e){
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping
	public ResponseEntity<?> update(
		UpdateCheckList dto
	){
		checkListService.update(dto);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	public ResponseEntity<?> delete(UUID id){
		checkListService.delete(id);
		return ResponseEntity.ok().build();
	}
}
