package com.life.jeonggiju.domain.numberRecord.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.exception.CategoryNotFoundException;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.numberRecord.exception.NumberRecordNotFoundException;
import com.life.jeonggiju.domain.numberRecord.dto.FindNumberAllResponse;
import com.life.jeonggiju.domain.numberRecord.dto.FindNumberResponse;
import com.life.jeonggiju.domain.numberRecord.dto.SaveNumber;
import com.life.jeonggiju.domain.numberRecord.dto.UpdateNumber;
import com.life.jeonggiju.domain.numberRecord.entity.NumberRecord;
import com.life.jeonggiju.domain.numberRecord.repository.NumberRepository;
import com.life.jeonggiju.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NumberService {

	private final NumberRepository numberRepository;
	private final CategoryRepository categoryRepository;

	public FindNumberResponse find(UUID numberId) {
		NumberRecord numberRecord = numberRepository.findById(numberId)
			.orElseThrow(() -> NumberRecordNotFoundException.withId(numberId));
		return FindNumberResponse.builder()
			.id(numberRecord.getId())
			.number(numberRecord.getNumber())
			.date(numberRecord.getDate())
			.build();
	}

	public FindNumberAllResponse findAll(UUID categoryId) {
		List<NumberRecord> allByCategoryId = numberRepository.findAllByCategory_Id(categoryId);
		User user = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new).getUser();

		List<FindNumberAllResponse.Content> result = new ArrayList();
		for (NumberRecord numberRecord : allByCategoryId) {
			result.add(FindNumberAllResponse.Content.builder()
				.id(numberRecord.getId())
				.number(numberRecord.getNumber())
				.date(numberRecord.getDate())
				.build());
		}

		return FindNumberAllResponse.builder()
			.birthYear(user.getBirthYear())
			.birthMonth(user.getBirthMonth())
			.birthDay(user.getBirthDay())
			.contents(result).build();
	}

	public FindNumberResponse findByDate(UUID categoryId, LocalDate date) {
		NumberRecord numberRecord = numberRepository.findByCategoryIdAndDate(categoryId, date)
			.orElseThrow(() -> NumberRecordNotFoundException.withCategoryIdAndDate(categoryId, date));
		return FindNumberResponse.builder()
			.id(numberRecord.getId())
			.date(numberRecord.getDate())
			.number(numberRecord.getNumber()).build();
	}

	@Transactional
	public void save(SaveNumber dto) {
		UUID id = dto.getCategoryId();
		Category category = categoryRepository.findById(id)
			.orElseThrow(() -> CategoryNotFoundException.withId(id));
		NumberRecord numberRecord = NumberRecord.of(category, dto.getNumber(), dto.getDate());
		numberRepository.save(numberRecord);
	}

	@Transactional
	public void update(UpdateNumber dto) {
		UUID id = dto.getId();
		NumberRecord NumberRecord = numberRepository.findById(id)
			.orElseThrow(() -> NumberRecordNotFoundException.withId(id));
		NumberRecord.update(dto.getNumber(), dto.getDate());
		numberRepository.save(NumberRecord);
	}

	@Transactional
	public void delete(UUID id) {
		numberRepository.deleteById(id);
	}
}
