package com.study.jeonggiju.domain.diet.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.diet.dto.DietDto;
import com.study.jeonggiju.domain.diet.entity.Diet;
import com.study.jeonggiju.domain.diet.service.DietService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/diet")
@RestController
@RequiredArgsConstructor
public class DietController {

	private final DietService service;

	@GetMapping("/all")
	public List<Diet> findAll() {
		return service.findAll();
	}

	@PostMapping
	public Diet save(DietDto dto) {
		return service.save(dto);
	}

	@PutMapping
	public Diet update(DietDto dto) {
		return service.update(dto);
	}

	@DeleteMapping
	public void delete(LocalDate date) {
		service.delete(date);
	}
}
