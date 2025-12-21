package com.study.jeonggiju.domain.caffiene.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.caffiene.dto.CaffeineDto;
import com.study.jeonggiju.domain.caffiene.entity.Caffeine;
import com.study.jeonggiju.domain.caffiene.service.CaffeineService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/caffeine")
@RestController
@RequiredArgsConstructor
public class CaffeineController {

	private final CaffeineService service;


	@GetMapping("/all")
	public List<Caffeine> findAll() {
		return service.findAll();
	}

	@PostMapping
	public Caffeine save(CaffeineDto dto) {
		return service.save(dto);
	}

	@PutMapping
	public Caffeine update(CaffeineDto dto) {
		return service.update(dto);
	}

	@DeleteMapping
	public void delete(Long id) {
		service.delete(id);
	}
}
