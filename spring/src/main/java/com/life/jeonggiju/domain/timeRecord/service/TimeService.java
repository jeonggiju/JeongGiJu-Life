package com.life.jeonggiju.domain.timeRecord.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.exception.CategoryNotFoundException;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.timeRecord.dto.FindTimeAllResponse;
import com.life.jeonggiju.domain.timeRecord.dto.FindTimeResponse;
import com.life.jeonggiju.domain.timeRecord.dto.SaveTime;
import com.life.jeonggiju.domain.timeRecord.dto.UpdateTime;
import com.life.jeonggiju.domain.timeRecord.entity.TimeRecord;
import com.life.jeonggiju.domain.timeRecord.repository.TimeRepository;
import com.life.jeonggiju.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeService {

	private final TimeRepository timeRepository;
	private final CategoryRepository categoryRepository;

	public FindTimeResponse find(UUID timeId) {
		TimeRecord timeRecord = timeRepository.findById(timeId).orElseThrow();
		return FindTimeResponse.builder()
			.id(timeRecord.getId())
			.time(timeRecord.getTime())
			.date(timeRecord.getDate())
			.build();
	}

	public FindTimeAllResponse findAll(UUID categoryId) {
		List<TimeRecord> allByCategoryId = timeRepository.findAllByCategory_Id(categoryId);
		User user = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new).getUser();

		List<FindTimeAllResponse.Content> result = new ArrayList();
		for (TimeRecord timeRecord : allByCategoryId) {
			result.add(FindTimeAllResponse.Content.builder()
				.id(timeRecord.getId())
				.time(timeRecord.getTime())
				.date(timeRecord.getDate())
				.build());
		}

		return FindTimeAllResponse.builder()
			.contents(result)
			.birthYear(user.getBirthYear())
			.birthMonth(user.getBirthMonth())
			.birthDay(user.getBirthDay())
			.build();
	}

	public FindTimeResponse findByDate(UUID categoryId, LocalDate date) {
		TimeRecord timeRecord = timeRepository.findByCategoryIdAndDate(categoryId, date).orElseThrow();
		return FindTimeResponse.builder()
			.id(timeRecord.getId())
			.time(timeRecord.getTime())
			.date(timeRecord.getDate())
			.build();
	}

	@Transactional
	public void save(SaveTime dto) {
		UUID id = dto.getCategoryId();
		Category category = categoryRepository.findById(id).orElseThrow();
		TimeRecord timeRecord = TimeRecord.of(category, dto.getTime(), dto.getDate());
		timeRepository.save(timeRecord);
	}

	@Transactional
	public void update(UpdateTime dto) {
		UUID id = dto.getId();
		TimeRecord timeRecord = timeRepository.findById(id).orElseThrow();
		timeRecord.update(dto.getTime(), dto.getDate());
		timeRepository.save(timeRecord);
	}

	@Transactional
	public void delete(UUID id) {
		timeRepository.deleteById(id);
	}
}
