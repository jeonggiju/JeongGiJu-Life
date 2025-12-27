package com.study.jeonggiju.domain.checkRecord.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.category.repository.CategoryRepository;
import com.study.jeonggiju.domain.checkRecord.dto.SaveCheck;
import com.study.jeonggiju.domain.checkRecord.dto.UpdateCheck;
import com.study.jeonggiju.domain.checkRecord.entity.CheckRecord;
import com.study.jeonggiju.domain.checkRecord.repository.CheckRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckService {

	private final CheckRepository checkRepository;
	private final CategoryRepository categoryRepository;

	public CheckRecord find(UUID checkId){
		return checkRepository.findById(checkId).orElseThrow();
	}

	public List<CheckRecord> findAll(UUID categoryId){
		return checkRepository.findAllByCategoryId(categoryId);
	}

	@Transactional
	public void save(SaveCheck dto){
		Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow();
		CheckRecord checkRecord = CheckRecord.of(category, dto.isSuccess());
		checkRepository.save(checkRecord);
	}

	@Transactional
	public void update(UpdateCheck dto){
		UUID id = dto.getId();
		CheckRecord checkRecord = checkRepository.findById(id).orElseThrow();
		checkRecord.update(dto.isSuccess());
		checkRepository.save(checkRecord);
	}

	@Transactional
	public void delete(UUID id){
		checkRepository.deleteById(id);
	}
}
