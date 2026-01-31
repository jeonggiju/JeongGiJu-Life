package com.life.jeonggiju.domain.expenseRecord.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.expenseRecord.dto.FindExpenseTagResponse;
import com.life.jeonggiju.domain.expenseRecord.dto.SaveExpenseTag;
import com.life.jeonggiju.domain.expenseRecord.dto.UpdateExpenseTag;
import com.life.jeonggiju.domain.expenseRecord.entity.ExpenseTag;
import com.life.jeonggiju.domain.expenseRecord.exception.ExpenseTagNotFoundException;
import com.life.jeonggiju.domain.expenseRecord.repository.ExpenseTagRepository;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseTagService {

	private final ExpenseTagRepository expenseTagRepository;
	private final UserRepository userRepository;

	public List<FindExpenseTagResponse> findAllByUser(UUID userId) {
		List<ExpenseTag> tags = expenseTagRepository.findAllByUser_Id(userId);
		return tags.stream()
			.map(tag -> FindExpenseTagResponse.builder()
				.id(tag.getId())
				.name(tag.getName())
				.build())
			.toList();
	}

	public FindExpenseTagResponse find(UUID tagId) {
		ExpenseTag tag = expenseTagRepository.findById(tagId)
			.orElseThrow(() -> ExpenseTagNotFoundException.withId(tagId));
		return FindExpenseTagResponse.builder()
			.id(tag.getId())
			.name(tag.getName())
			.build();
	}

	@Transactional
	public void save(UUID userId, SaveExpenseTag dto) {
		User user = userRepository.findById(userId)
			.orElseThrow(UserNotFoundException::new);
		ExpenseTag tag = ExpenseTag.of(user, dto.getName());
		expenseTagRepository.save(tag);
	}

	@Transactional
	public void update(UpdateExpenseTag dto) {
		ExpenseTag tag = expenseTagRepository.findById(dto.getId())
			.orElseThrow(() -> ExpenseTagNotFoundException.withId(dto.getId()));
		tag.update(dto.getName());
		expenseTagRepository.save(tag);
	}

	@Transactional
	public void delete(UUID id) {
		expenseTagRepository.deleteById(id);
	}
}
