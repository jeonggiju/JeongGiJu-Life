package com.study.jeonggiju.domain.checkList.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.category.repository.CategoryRepository;
import com.study.jeonggiju.domain.checkList.dto.FindCheckListResponse;
import com.study.jeonggiju.domain.checkList.dto.SaveCheckList;
import com.study.jeonggiju.domain.checkList.dto.UpdateCheckList;
import com.study.jeonggiju.domain.checkList.entity.CheckListRecord;
import com.study.jeonggiju.domain.checkList.repository.CheckListRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckListService {

	private final CheckListRepository checkListRepository;
	private final CategoryRepository categoryRepository;

	public FindCheckListResponse find(UUID checkId){
		CheckListRecord checkListRecord = checkListRepository.findById(checkId).orElseThrow();
		return FindCheckListResponse.builder().id(checkListRecord.getId()).success(checkListRecord.isSuccess()).date(
			checkListRecord.getDate()).build();
	}

	public List<FindCheckListResponse> findAll(UUID categoryId){
		List<CheckListRecord> allByCategoryId = checkListRepository.findAllByCategory_Id(categoryId);

		List<FindCheckListResponse> result = new ArrayList();
		for(CheckListRecord checkListRecord : allByCategoryId) {
			FindCheckListResponse response = FindCheckListResponse.builder().id(checkListRecord.getId()).success(
				checkListRecord.isSuccess()).todo(checkListRecord.getTodo()).date(checkListRecord.getDate()).build();
			result.add(response);
		}
		return result;
	}

	public List<CheckListRecord> findByDate(UUID categoryId ,LocalDate date){
		return checkListRepository.findByCategoryIdAndDate(categoryId, date).orElseThrow();
	}

	@Transactional
	public void save(SaveCheckList dto){
		Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow();
		CheckListRecord checkListRecord = CheckListRecord.of(category, dto.getTodo(),dto.isSuccess(), dto.getDate());
		checkListRepository.save(checkListRecord);
	}

	@Transactional
	public void update(UpdateCheckList dto){
		UUID id = dto.getId();
		CheckListRecord checkListRecord = checkListRepository.findById(id).orElseThrow();
		checkListRecord.update( dto.getTodo(), dto.isSuccess(), dto.getDate());
		checkListRepository.save(checkListRecord);
	}

	@Transactional
	public void delete(UUID id){
		checkListRepository.deleteById(id);
	}
}
