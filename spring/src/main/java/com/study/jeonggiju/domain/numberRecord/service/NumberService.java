package com.study.jeonggiju.domain.numberRecord.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.category.repository.CategoryRepository;
import com.study.jeonggiju.domain.numberRecord.dto.FindNumberResponse;
import com.study.jeonggiju.domain.numberRecord.dto.SaveNumber;
import com.study.jeonggiju.domain.numberRecord.dto.UpdateNumber;
import com.study.jeonggiju.domain.numberRecord.entity.NumberRecord;
import com.study.jeonggiju.domain.numberRecord.repository.NumberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NumberService {

	private final NumberRepository numberRepository;
	private final CategoryRepository categoryRepository;

	public FindNumberResponse find(UUID numberId){
		NumberRecord numberRecord = numberRepository.findById(numberId).orElseThrow();
		return FindNumberResponse.builder()
			.id(numberRecord.getId())
			.number(numberRecord.getNumber())
			.date(numberRecord.getDate())
			.build();
	}

	public List<FindNumberResponse> findAll(UUID categoryId){
		List<NumberRecord> allByCategoryId = numberRepository.findAllByCategory_Id(categoryId);
		List<FindNumberResponse> result = new ArrayList();
		for(NumberRecord numberRecord : allByCategoryId) {
			result.add(FindNumberResponse.builder().id(numberRecord.getId()).number(numberRecord.getNumber()).date(numberRecord.getDate()).build());
		}
		return result;
	}

	public NumberRecord findByDate(UUID categoryId , LocalDate date){
		return numberRepository.findByCategoryIdAndDate(categoryId, date).orElseThrow();
	}

	@Transactional
	public void save(SaveNumber dto){
		UUID id = dto.getCategoryId();
		Category category = categoryRepository.findById(id).orElseThrow();
		NumberRecord numberRecord = NumberRecord.of(category,dto.getNumber() ,dto.getDate());
		numberRepository.save(numberRecord);
	}

	@Transactional
	public void update(UpdateNumber dto){
		UUID id = dto.getId();
		NumberRecord NumberRecord = numberRepository.findById(id).orElseThrow();
		NumberRecord.update(dto.getNumber(),dto.getDate());
		numberRepository.save(NumberRecord);
	}

	@Transactional
	public void delete(UUID id){
		numberRepository.deleteById(id);
	}
}
