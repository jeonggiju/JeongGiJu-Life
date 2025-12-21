package com.study.jeonggiju.domain.sex.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.sex.dto.SexDto;
import com.study.jeonggiju.domain.sex.entity.Sex;
import com.study.jeonggiju.domain.sex.service.SexService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/sex")
@RestController
@RequiredArgsConstructor
public class SexController {

	private final SexService service;

	@GetMapping("/all")
	public List<Sex> findAll() {
		return service.findAll();
	}

	@PostMapping
	public Sex save(SexDto dto) {
		return service.save(dto);
	}

	@PutMapping
	public Sex update(SexDto dto) {
		return service.update(dto);
	}

	@DeleteMapping
	public void delete(Long id) {
		service.delete(id);
	}
}
