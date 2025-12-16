package com.study.jeonggiju.domain.sleep.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.sleep.dto.SleepDto;
import com.study.jeonggiju.domain.sleep.entity.Sleep;
import com.study.jeonggiju.domain.sleep.repository.SleepRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SleepService {

	private final SleepRepository sleepRepository;

	public Sleep save(SleepDto dto) {
		Sleep sleep = Sleep.from(dto);
		return sleepRepository.save(sleep);
	}

	public Sleep update(SleepDto dto) {
		Sleep sleep = sleepRepository.findByDate(dto.getDate())
			.orElseThrow(() -> new IllegalArgumentException("데이터가 존재하지 않습니다."));

		sleep.update(dto);
		return sleepRepository.save(sleep);
	}

	public void delete(LocalDate date) {
		sleepRepository.deleteByDate(date);
	}

	public List<Sleep> findAll() {
		return sleepRepository.findAll();
	}

}
