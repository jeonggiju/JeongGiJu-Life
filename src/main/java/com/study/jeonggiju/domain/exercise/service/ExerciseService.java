package com.study.jeonggiju.domain.exercise.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.exercise.dto.ExerciseDto;
import com.study.jeonggiju.domain.exercise.entity.Exercise;
import com.study.jeonggiju.domain.exercise.repository.ExerciseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseService {

	private final ExerciseRepository repository;

	public Exercise save(ExerciseDto dto) {
		try{
			Exercise exercise = Exercise.from(dto);
			return repository.save(exercise);
		}catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("이미 존재하는 날짜임");
		}
	}

	public List<Exercise> findAll() {
		return repository.findAll();
	}

	public Exercise update(ExerciseDto dto) {
		Exercise exercise = repository.findByDate(dto.getDate())
			.orElseThrow(() -> new IllegalArgumentException("해당 날짜 데이터 없음"));
		exercise.update(dto);
		return repository.save(exercise);
	}

	public void delete(Long id) {
		repository.deleteById(id);
	}
}
