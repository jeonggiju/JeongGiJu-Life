package com.study.jeonggiju.domain.timeRecord.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.category.repository.CategoryRepository;
import com.study.jeonggiju.domain.timeRecord.dto.SaveTime;
import com.study.jeonggiju.domain.timeRecord.dto.UpdateTime;
import com.study.jeonggiju.domain.timeRecord.entity.TimeRecord;
import com.study.jeonggiju.domain.timeRecord.repository.TimeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeService {

	private final TimeRepository timeRepository;
	private final CategoryRepository categoryRepository;

	public TimeRecord find(UUID timeId){
		return timeRepository.findById(timeId).orElseThrow();
	}

	public List<TimeRecord> findAll(UUID categoryId){
		return timeRepository.findAllByCategory_Id(categoryId);
	}

	@Transactional
	public void save(SaveTime dto){
		UUID id = dto.getCategoryId();
		Category category = categoryRepository.findById(id).orElseThrow();
		TimeRecord timeRecord = TimeRecord.of(category,dto.getTime() ,dto.getDate());
		timeRepository.save(timeRecord);
	}

	@Transactional
	public void update(UpdateTime dto){
		UUID id = dto.getId();
		TimeRecord timeRecord = timeRepository.findById(id).orElseThrow();
		timeRecord.update(dto.getTime(),dto.getDate());
		timeRepository.save(timeRecord);
	}

	@Transactional
	public void delete(UUID id){
		timeRepository.deleteById(id);
	}
}
