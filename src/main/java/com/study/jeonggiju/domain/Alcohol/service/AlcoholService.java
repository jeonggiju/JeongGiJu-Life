package com.study.jeonggiju.domain.Alcohol.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.Alcohol.dto.AlcoholDto;
import com.study.jeonggiju.domain.Alcohol.entity.Alcohol;
import com.study.jeonggiju.domain.Alcohol.repository.AlcoholRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlcoholService {

	private final AlcoholRepository repository;

	public Alcohol save(AlcoholDto dto) {
		try{
			Alcohol caffeine = Alcohol.from(dto);
			return repository.save(caffeine);
		}catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("이미 존재하는 날짜");
		}
	}

	public List<Alcohol> findAll() {
		return repository.findAll();
	}

	public Alcohol update(AlcoholDto dto) {
		Alcohol caffeine = repository.findByDate(dto.getDate())
			.orElseThrow(() -> new IllegalArgumentException("해당 날짜 데이터 없음"));
		caffeine.update(dto);
		return repository.save(caffeine);
	}

	public void delete(Long id) {
		repository.deleteById(id);
	}
}
