package com.life.jeonggiju.domain.textRecord.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.textRecord.dto.FindTextResponse;
import com.life.jeonggiju.domain.textRecord.dto.SaveText;
import com.life.jeonggiju.domain.textRecord.dto.UpdateText;
import com.life.jeonggiju.domain.textRecord.entity.TextRecord;
import com.life.jeonggiju.domain.textRecord.repository.TextRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextService {

	private final CategoryRepository categoryRepository;
	private final TextRepository textRepository;

	public FindTextResponse find(UUID textId){
		TextRecord textRecord = textRepository.findById(textId).orElseThrow();
		FindTextResponse response = FindTextResponse.builder()
			.id(textRecord.getId())
			.title(textRecord.getTitle())
			.text(textRecord.getText())
			.date(textRecord.getDate()).build();
		return response;
	}

	public List<FindTextResponse> findByDate(UUID categoryId ,LocalDate date){
		List<TextRecord> textRecords = textRepository.findByCategoryIdAndDate(categoryId, date).orElseThrow();
		List<FindTextResponse> result = new ArrayList();
		for(TextRecord textRecord : textRecords) {
			result.add(FindTextResponse.builder().id(textRecord.getId()).title(textRecord.getTitle()).text(textRecord.getText()).date(textRecord.getDate()).build());
		}
		return result;
	}

	public List<FindTextResponse> findAll(UUID categoryId){
		List<TextRecord> allByCategoryId = textRepository.findAllByCategory_Id(categoryId);
		List<FindTextResponse> result = new ArrayList();
		for(TextRecord textRecord : allByCategoryId) {
			result.add(FindTextResponse.builder().id(textRecord.getId()).title(textRecord.getTitle()).text(textRecord.getText()).date(textRecord.getDate()).build());
		}
		return result;

	}

	@Transactional
	public void save(SaveText dto){
		UUID categoryId = dto.getCategoryId();
		Category category = categoryRepository.findById(categoryId).orElseThrow();
		TextRecord textRecord = TextRecord.of(category, dto.getTitle(), dto.getText(), dto.getDate());
		textRepository.save(textRecord);
	}

	@Transactional
	public void update(UpdateText dto){
		UUID id = dto.getId();
		TextRecord textRecord = textRepository.findById(id).orElseThrow();
		textRecord.update(dto.getTitle(), dto.getText(), dto.getDate());
		textRepository.save(textRecord);
	}

	@Transactional
	public void delete(UUID id){
		textRepository.deleteById(id);
	}
}
