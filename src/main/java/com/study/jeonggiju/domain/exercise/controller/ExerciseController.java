package com.study.jeonggiju.domain.exercise.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.exercise.dto.ExerciseDto;
import com.study.jeonggiju.domain.exercise.entity.Exercise;
import com.study.jeonggiju.domain.exercise.service.ExerciseService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/exercise")
@RestController
@RequiredArgsConstructor
public class ExerciseController {

	private final ExerciseService service;

	@GetMapping("/all")
	public List<Exercise> findAll() {
		return service.findAll();
	}

	@PostMapping
	public Exercise save(ExerciseDto dto) {
		return service.save(dto);
	}

	@PutMapping
	public Exercise update(ExerciseDto dto) {
		return service.update(dto);
	}

	@DeleteMapping
	public void delete(Long id) {
		service.delete(id);
	}
}
