package com.life.jeonggiju.domain.checkRecord.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.exception.CategoryNotFoundException;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.checkRecord.exception.CheckRecordNotFoundException;
import com.life.jeonggiju.domain.checkRecord.dto.FindCheckAllResponse;
import com.life.jeonggiju.domain.checkRecord.dto.FindCheckResponse;
import com.life.jeonggiju.domain.checkRecord.dto.SaveCheck;
import com.life.jeonggiju.domain.checkRecord.dto.UpdateCheck;
import com.life.jeonggiju.domain.checkRecord.entity.CheckRecord;
import com.life.jeonggiju.domain.checkRecord.repository.CheckRepository;
import com.life.jeonggiju.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckService {

	private final CheckRepository checkRepository;
	private final CategoryRepository categoryRepository;

	public FindCheckResponse find(UUID checkId) {
		CheckRecord checkRecord = checkRepository.findById(checkId)
			.orElseThrow(() -> CheckRecordNotFoundException.withId(checkId));
		return FindCheckResponse.builder()
			.id(checkRecord.getId())
			.success(checkRecord.isSuccess())
			.date(checkRecord.getDate())
			.build();
	}

	public FindCheckAllResponse findAll(UUID categoryId) {
		List<CheckRecord> allByCategoryId = checkRepository.findAllByCategory_Id(categoryId);
		User user = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new).getUser();

		int year = user.getBirthYear();
		int month = user.getBirthMonth();
		int day = user.getBirthDay();

		List<FindCheckAllResponse.Content> result = new ArrayList();
		for (CheckRecord checkRecord : allByCategoryId) {
			FindCheckAllResponse.Content response = FindCheckAllResponse.Content.builder()
				.id(checkRecord.getId())
				.success(checkRecord.isSuccess())
				.date(checkRecord.getDate())
				.build();
			result.add(response);
		}

		return FindCheckAllResponse.builder().contents(result).birthYear(year).birthMonth(month).birthDay(day).build();
	}

	public FindCheckResponse findByDate(UUID categoryId, LocalDate date) {
		CheckRecord checkRecord = checkRepository.findByCategoryIdAndDate(categoryId, date)
			.orElseThrow(() -> CheckRecordNotFoundException.withCategoryIdAndDate(categoryId, date));
		return FindCheckResponse.builder()
			.id(checkRecord.getId())
			.date(checkRecord.getDate())
			.success(checkRecord.isSuccess())
			.build();
	}

	@Transactional
	public void save(SaveCheck dto) {
		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(() -> CategoryNotFoundException.withId(dto.getCategoryId()));
		CheckRecord checkRecord = CheckRecord.of(category, dto.isSuccess(), dto.getDate());
		checkRepository.save(checkRecord);
	}

	@Transactional
	public void update(UpdateCheck dto) {
		UUID id = dto.getId();
		CheckRecord checkRecord = checkRepository.findById(id)
			.orElseThrow(() -> CheckRecordNotFoundException.withId(id));
		checkRecord.update(dto.isSuccess(), dto.getDate());
		checkRepository.save(checkRecord);
	}

	@Transactional
	public void delete(UUID id) {
		checkRepository.deleteById(id);
	}
}
