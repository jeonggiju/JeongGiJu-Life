package com.study.jeonggiju.domain.smoking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.smoking.dto.SmokingDto;
import com.study.jeonggiju.domain.smoking.entity.Smoking;
import com.study.jeonggiju.domain.smoking.service.SmokingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/smoking")
@RequiredArgsConstructor
public class SmokingController {

	private final SmokingService smokingService;

	@GetMapping("/all")
	public List<Smoking> findAll() {
		return smokingService.findAll();
	}

	@PutMapping
	public Smoking update(SmokingDto dto) {
		return smokingService.update(dto);
	}

	@DeleteMapping
	public void delete(Long id) {
		smokingService.delete(id);
	}

	@PostMapping
	public Smoking save(SmokingDto dto) {
		return smokingService.save(dto);
	}
}
