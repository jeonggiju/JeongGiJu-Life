package com.study.jeonggiju.domain.category.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.study.jeonggiju.domain.category.dto.AddCategory;
import com.study.jeonggiju.domain.category.dto.FindCategoryResponse;
import com.study.jeonggiju.domain.category.dto.UpdateCategory;
import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.category.repository.CategoryRepository;
import com.study.jeonggiju.domain.user.entity.User;
import com.study.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;

	public FindCategoryResponse find(UUID categoryId){
		Category category = categoryRepository.findById(categoryId).orElseThrow();
		return FindCategoryResponse
			.builder()
			.id(category.getId())
			.title(category.getTitle())
			.description(category.getDescription())
			.recordType(category.getRecordType())
			.build();

	}

	public List<FindCategoryResponse> findAll(UUID userId){
		List<Category> categories = categoryRepository.findAllByUserId(userId).orElseThrow();
		List<FindCategoryResponse> result = new ArrayList();
		for(Category category : categories) {
			result.add(FindCategoryResponse
				.builder()
				.id(category.getId())
				.title(category.getTitle())
				.description(category.getDescription())
				.recordType(category.getRecordType())
				.build());
		}
		return result;

	}

	public void save(UUID userId, AddCategory addCategory){
		User user = userRepository.findById(userId).orElseThrow();
		Category category = Category.of(addCategory.getTitle(), addCategory.getDescription(), addCategory.getRecordType(), user);
		categoryRepository.save(category);
	}

	public void update(UpdateCategory updateCategory){
		Category category = categoryRepository.findById(updateCategory.getId()).orElseThrow();
		category.update(updateCategory.getTitle(), updateCategory.getDescription());
		categoryRepository.save(category);
	}

	public void delete(UUID id){
		categoryRepository.deleteById(id);
	}
}
