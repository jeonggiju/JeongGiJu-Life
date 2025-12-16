package com.study.jeonggiju.domain.smoking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.smoking.dto.SmokingDto;
import com.study.jeonggiju.domain.smoking.entity.Smoking;
import com.study.jeonggiju.domain.smoking.repository.SmokingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmokingService {

	private final SmokingRepository smokingRepository;

	public Smoking save(SmokingDto dto) {
		Smoking smoking = Smoking.from(dto);
		return smokingRepository.save(smoking);
	}

	public Smoking update(SmokingDto dto) {
		Smoking smoking = smokingRepository.findByDate(dto.getDate())
			.orElseThrow(() -> new IllegalArgumentException("데이터가 없습니다."));
		smoking.update(dto);
		return smokingRepository.save(smoking);
	}

	public void delete(LocalDate date) {
		smokingRepository.deleteByDate(date);
	}

	public List<Smoking> findAll(){
		return smokingRepository.findAll();
	}
}
