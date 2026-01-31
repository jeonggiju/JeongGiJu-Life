package com.life.jeonggiju.domain.expenseRecord.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.exception.CategoryNotFoundException;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.expenseRecord.dto.FindExpenseAllResponse;
import com.life.jeonggiju.domain.expenseRecord.dto.FindExpenseResponse;
import com.life.jeonggiju.domain.expenseRecord.dto.SaveExpense;
import com.life.jeonggiju.domain.expenseRecord.dto.UpdateExpense;
import com.life.jeonggiju.domain.expenseRecord.entity.ExpenseRecord;
import com.life.jeonggiju.domain.expenseRecord.entity.ExpenseTag;
import com.life.jeonggiju.domain.expenseRecord.exception.ExpenseRecordNotFoundException;
import com.life.jeonggiju.domain.expenseRecord.repository.ExpenseRepository;
import com.life.jeonggiju.domain.expenseRecord.repository.ExpenseTagRepository;
import com.life.jeonggiju.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

	private final ExpenseRepository expenseRepository;
	private final ExpenseTagRepository expenseTagRepository;
	private final CategoryRepository categoryRepository;

	public FindExpenseResponse find(UUID expenseId) {
		ExpenseRecord expenseRecord = expenseRepository.findById(expenseId)
			.orElseThrow(() -> ExpenseRecordNotFoundException.withId(expenseId));

		List<FindExpenseResponse.TagInfo> tagInfos = expenseRecord.getTags().stream()
			.map(rt -> FindExpenseResponse.TagInfo.builder()
				.id(rt.getExpenseTag().getId())
				.name(rt.getExpenseTag().getName())
				.build())
			.toList();

		return FindExpenseResponse.builder()
			.id(expenseRecord.getId())
			.amount(expenseRecord.getAmount())
			.expenseType(expenseRecord.getExpenseType())
			.paymentMethod(expenseRecord.getPaymentMethod())
			.merchant(expenseRecord.getMerchant())
			.memo(expenseRecord.getMemo())
			.date(expenseRecord.getDate())
			.tags(tagInfos)
			.build();
	}

	public FindExpenseAllResponse findAll(UUID categoryId) {
		List<ExpenseRecord> allByCategoryId = expenseRepository.findAllByCategory_Id(categoryId);
		Category category = categoryRepository.findById(categoryId)
			.orElseThrow(CategoryNotFoundException::new);
		User user = category.getUser();

		List<FindExpenseAllResponse.Content> contents = new ArrayList<>();
		for (ExpenseRecord record : allByCategoryId) {
			List<FindExpenseAllResponse.TagInfo> tagInfos = record.getTags().stream()
				.map(rt -> FindExpenseAllResponse.TagInfo.builder()
					.id(rt.getExpenseTag().getId())
					.name(rt.getExpenseTag().getName())
					.build())
				.toList();

			FindExpenseAllResponse.Content content = FindExpenseAllResponse.Content.builder()
				.id(record.getId())
				.amount(record.getAmount())
				.expenseType(record.getExpenseType())
				.paymentMethod(record.getPaymentMethod())
				.merchant(record.getMerchant())
				.memo(record.getMemo())
				.date(record.getDate())
				.tags(tagInfos)
				.build();
			contents.add(content);
		}

		return FindExpenseAllResponse.builder()
			.birthYear(user.getBirthYear())
			.birthMonth(user.getBirthMonth())
			.birthDay(user.getBirthDay())
			.contents(contents)
			.build();
	}

	public List<ExpenseRecord> findByDate(UUID categoryId, LocalDate date) {
		return expenseRepository.findByCategoryIdAndDate(categoryId, date)
			.orElseThrow(() -> ExpenseRecordNotFoundException.withCategoryIdAndDate(categoryId, date));
	}

	@Transactional
	public void save(SaveExpense dto) {
		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(() -> CategoryNotFoundException.withId(dto.getCategoryId()));

		ExpenseRecord expenseRecord = ExpenseRecord.of(
			category,
			dto.getAmount(),
			dto.getExpenseType(),
			dto.getPaymentMethod(),
			dto.getMerchant(),
			dto.getMemo(),
			dto.getDate()
		);

		if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
			List<ExpenseTag> tags = expenseTagRepository.findAllById(dto.getTagIds());
			for (ExpenseTag tag : tags) {
				expenseRecord.addTag(tag);
			}
		}

		expenseRepository.save(expenseRecord);
	}

	@Transactional
	public void update(UpdateExpense dto) {
		ExpenseRecord expenseRecord = expenseRepository.findById(dto.getId())
			.orElseThrow(() -> ExpenseRecordNotFoundException.withId(dto.getId()));

		expenseRecord.update(
			dto.getAmount(),
			dto.getExpenseType(),
			dto.getPaymentMethod(),
			dto.getMerchant(),
			dto.getMemo(),
			dto.getDate()
		);

		expenseRecord.clearTags();
		if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
			List<ExpenseTag> tags = expenseTagRepository.findAllById(dto.getTagIds());
			for (ExpenseTag tag : tags) {
				expenseRecord.addTag(tag);
			}
		}

		expenseRepository.save(expenseRecord);
	}

	@Transactional
	public void delete(UUID id) {
		expenseRepository.deleteById(id);
	}
}
