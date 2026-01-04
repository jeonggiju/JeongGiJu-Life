package com.life.jeonggiju.domain.checkRecord.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.checkRecord.dto.FindCheckResponse;
import com.life.jeonggiju.domain.checkRecord.dto.SaveCheck;
import com.life.jeonggiju.domain.checkRecord.dto.UpdateCheck;
import com.life.jeonggiju.domain.checkRecord.entity.CheckRecord;
import com.life.jeonggiju.domain.checkRecord.repository.CheckRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckService {

	private final CheckRepository checkRepository;
	private final CategoryRepository categoryRepository;

	public FindCheckResponse find(UUID checkId){
		CheckRecord checkRecord = checkRepository.findById(checkId).orElseThrow();
		return FindCheckResponse.builder().id(checkRecord.getId()).success(checkRecord.isSuccess()).date(checkRecord.getDate()).build();
	}

	public List<FindCheckResponse> findAll(UUID categoryId){
		List<CheckRecord> allByCategoryId = checkRepository.findAllByCategory_Id(categoryId);

		List<FindCheckResponse> result = new ArrayList();
		for(CheckRecord checkRecord : allByCategoryId) {
			FindCheckResponse response = FindCheckResponse.builder().id(checkRecord.getId()).success(checkRecord.isSuccess()).date(checkRecord.getDate()).build();
			result.add(response);
		}
		return result;
	}

	public CheckRecord findByDate(UUID categoryId ,LocalDate date){
		return checkRepository.findByCategoryIdAndDate(categoryId, date).orElseThrow();
	}

	@Transactional
	public void save(SaveCheck dto){
		Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow();
		CheckRecord checkRecord = CheckRecord.of(category, dto.isSuccess(), dto.getDate());
		checkRepository.save(checkRecord);
	}

	@Transactional
	public void update(UpdateCheck dto){
		UUID id = dto.getId();
		CheckRecord checkRecord = checkRepository.findById(id).orElseThrow();
		checkRecord.update(dto.isSuccess(), dto.getDate());
		checkRepository.save(checkRecord);
	}

	@Transactional
	public void delete(UUID id){
		checkRepository.deleteById(id);
	}
}
