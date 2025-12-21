package com.study.jeonggiju.domain.sleep.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.jeonggiju.domain.caffiene.entity.Caffeine;
import com.study.jeonggiju.domain.sleep.dto.SleepDto;
import com.study.jeonggiju.domain.sleep.entity.Sleep;
import com.study.jeonggiju.domain.sleep.service.SleepService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sleep")
@RequiredArgsConstructor
public class SleepController {

	private final SleepService sleepService;

	@GetMapping("/all")
	public List<Sleep> findAll() {
		return sleepService.findAll();
	}

	@PostMapping
	public Sleep save(SleepDto dto){
		return sleepService.save(dto);
	}

	@PutMapping
	public Sleep update(SleepDto dto){
		return sleepService.update(dto);
	}

	@DeleteMapping
	public void delete(Long id){
		sleepService.delete(id);
	}

}
