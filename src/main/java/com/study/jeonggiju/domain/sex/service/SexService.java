package com.study.jeonggiju.domain.sex.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.sex.dto.SexDto;
import com.study.jeonggiju.domain.sex.entity.Sex;
import com.study.jeonggiju.domain.sex.repository.SexRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SexService {

	private final SexRepository repository;

	public Sex save(SexDto dto) {
		try{
			Sex exercise = Sex.from(dto);
			return repository.save(exercise);
		}catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("이미 존재하는 날짜임");
		}
	}

	public List<Sex> findAll() {
		return repository.findAll();
	}

	public Sex update(SexDto dto) {
		Sex exercise = repository.findByDate(dto.getDate())
			.orElseThrow(() -> new IllegalArgumentException("해당 날짜 데이터 없음"));
		exercise.update(dto);
		return repository.save(exercise);
	}

	public void delete(Long id) {
		repository.deleteById(id);
	}
}
