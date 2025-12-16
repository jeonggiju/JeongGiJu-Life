package com.study.jeonggiju.domain.diet.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.diet.dto.DietDto;
import com.study.jeonggiju.domain.diet.entity.Diet;
import com.study.jeonggiju.domain.diet.repository.DietRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DietService {

	private final DietRepository repository;

	public Diet save(DietDto dto) {
		Diet caffeine = Diet.from(dto);
		return repository.save(caffeine);
	}

	public List<Diet> findAll() {
		return repository.findAll();
	}

	public Diet update(DietDto dto) {
		Diet caffeine = repository.findByDate(dto.getDate())
			.orElseThrow(() -> new IllegalArgumentException("해당 날짜 데이터 없음"));
		caffeine.update(dto);
		return repository.save(caffeine);
	}

	public void delete(LocalDate date) {
		repository.deleteByDate(date);
	}
}
