package com.study.jeonggiju.domain.textRecord.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.category.repository.CategoryRepository;
import com.study.jeonggiju.domain.textRecord.dto.SaveText;
import com.study.jeonggiju.domain.textRecord.dto.UpdateText;
import com.study.jeonggiju.domain.textRecord.entity.TextRecord;
import com.study.jeonggiju.domain.textRecord.repository.TextRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TextService {

	private final CategoryRepository categoryRepository;
	private final TextRepository textRepository;

	public TextRecord find(UUID textId){
		return textRepository.findById(textId).orElseThrow();
	}

	public List<TextRecord> findAll(UUID categoryId){
		return textRepository.findAllByCategoryId(categoryId);
	}

	@Transactional
	public void save(SaveText dto){
		UUID categoryId = dto.getCategoryId();
		Category category = categoryRepository.findById(categoryId).orElseThrow();
		TextRecord textRecord = TextRecord.of(category, dto.getTitle(), dto.getText());
		textRepository.save(textRecord);
	}

	@Transactional
	public void update(UpdateText dto){
		UUID id = dto.getId();
		TextRecord textRecord = textRepository.findById(id).orElseThrow();
		textRecord.update(dto.getTitle(), dto.getText());
		textRepository.save(textRecord);
	}

	@Transactional
	public void delete(UUID id){
		textRepository.deleteById(id);
	}
}
