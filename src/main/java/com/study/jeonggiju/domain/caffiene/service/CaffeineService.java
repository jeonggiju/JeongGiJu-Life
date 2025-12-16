package com.study.jeonggiju.domain.caffiene.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.caffiene.dto.CaffeineDto;
import com.study.jeonggiju.domain.caffiene.entity.Caffeine;
import com.study.jeonggiju.domain.caffiene.repository.CaffeineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaffeineService {

	private final CaffeineRepository repository;

	public Caffeine save(CaffeineDto dto) {
		Caffeine caffeine = Caffeine.from(dto);
		return repository.save(caffeine);
	}

	public List<Caffeine> findAll() {
		return repository.findAll();
	}

	public Caffeine update(CaffeineDto dto) {
		Caffeine caffeine = repository.findByDate(dto.getDate())
			.orElseThrow(() -> new IllegalArgumentException("해당 날짜 데이터 없음"));
		caffeine.update(dto);
		return repository.save(caffeine);
	}

	public void delete(LocalDate date) {
		repository.deleteByDate(date);
	}
}
