package com.study.jeonggiju.domain.Alcohol.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.Alcohol.dto.AlcoholDto;
import com.study.jeonggiju.domain.Alcohol.entity.Alcohol;
import com.study.jeonggiju.domain.Alcohol.service.AlcoholService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/alcohol")
@RestController
@RequiredArgsConstructor
public class AlcoholController {

	private final AlcoholService service;


	@GetMapping("/all")
	public List<Alcohol> findAll() {
		return service.findAll();
	}

	@PostMapping
	public Alcohol save(AlcoholDto dto) {
		return service.save(dto);
	}

	@PutMapping
	public Alcohol update(AlcoholDto dto) {
		return service.update(dto);
	}

	@DeleteMapping
	public void delete(Long id) {
		service.delete(id);
	}
}
