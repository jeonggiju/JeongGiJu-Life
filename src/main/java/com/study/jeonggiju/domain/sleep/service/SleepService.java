package com.study.jeonggiju.domain.sleep.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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
		try{
			Sleep sleep = Sleep.from(dto);
			return sleepRepository.save(sleep);
		}catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("이미 존재하는 날짜");
		}
	}

	public Sleep update(SleepDto dto) {
		Sleep sleep = sleepRepository.findByDate(dto.getDate())
			.orElseThrow(() -> new IllegalArgumentException("데이터가 존재하지 않습니다."));

		sleep.update(dto);
		return sleepRepository.save(sleep);
	}

	public void delete(Long id) {
		sleepRepository.deleteById(id);
	}

	public List<Sleep> findAll() {
		return sleepRepository.findAll();
	}

}
