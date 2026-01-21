package com.life.jeonggiju.domain.checkListRecord.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.exception.CategoryNotFoundException;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.checkListRecord.exception.CheckListRecordNotFoundException;
import com.life.jeonggiju.domain.checkListRecord.dto.FindCheckListAllResponse;
import com.life.jeonggiju.domain.checkListRecord.dto.FindCheckListResponse;
import com.life.jeonggiju.domain.checkListRecord.dto.SaveCheckList;
import com.life.jeonggiju.domain.checkListRecord.dto.UpdateCheckList;
import com.life.jeonggiju.domain.checkListRecord.entity.CheckListRecord;
import com.life.jeonggiju.domain.checkListRecord.repository.CheckListRepository;
import com.life.jeonggiju.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckListService {

	private final CheckListRepository checkListRepository;
	private final CategoryRepository categoryRepository;

	public FindCheckListResponse find(UUID checkId) {
		CheckListRecord checkListRecord = checkListRepository.findById(checkId)
			.orElseThrow(() -> CheckListRecordNotFoundException.withId(checkId));
		return FindCheckListResponse.builder().id(checkListRecord.getId()).success(checkListRecord.isSuccess()).date(
			checkListRecord.getDate()).build();
	}

	public FindCheckListAllResponse findAll(UUID categoryId) {
		List<CheckListRecord> allByCategoryId = checkListRepository.findAllByCategory_Id(categoryId);
		Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
		User user = category.getUser();
		int year = user.getBirthYear();
		int month = user.getBirthMonth();
		int day = user.getBirthDay();

		List<FindCheckListAllResponse.Content> result = new ArrayList();
		for (CheckListRecord checkListRecord : allByCategoryId) {
			FindCheckListAllResponse.Content build = FindCheckListAllResponse.Content.builder()
				.id(checkListRecord.getId())
				.todo(checkListRecord.getTodo())
				.success(checkListRecord.isSuccess())
				.date(checkListRecord.getDate()).build();
			result.add(build);
		}
		return FindCheckListAllResponse.builder()
			.contents(result)
			.birthYear(year)
			.birthMonth(month)
			.birthDay(day)
			.build();
	}

	public List<CheckListRecord> findByDate(UUID categoryId, LocalDate date) {
		return checkListRepository.findByCategoryIdAndDate(categoryId, date)
			.orElseThrow(() -> CheckListRecordNotFoundException.withCategoryIdAndDate(categoryId, date));
	}

	@Transactional
	public void save(SaveCheckList dto) {
		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(() -> CategoryNotFoundException.withId(dto.getCategoryId()));
		CheckListRecord checkListRecord = CheckListRecord.of(category, dto.getTodo(), dto.isSuccess(), dto.getDate());
		checkListRepository.save(checkListRecord);
	}

	@Transactional
	public void update(UpdateCheckList dto) {
		UUID id = dto.getId();
		CheckListRecord checkListRecord = checkListRepository.findById(id)
			.orElseThrow(() -> CheckListRecordNotFoundException.withId(id));
		checkListRecord.update(dto.getTodo(), dto.isSuccess(), dto.getDate());
		checkListRepository.save(checkListRecord);
	}

	@Transactional
	public void delete(UUID id) {
		checkListRepository.deleteById(id);
	}
}
