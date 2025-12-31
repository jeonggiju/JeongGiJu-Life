package com.study.jeonggiju.domain.category.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.jeonggiju.domain.category.dto.AddCategory;
import com.study.jeonggiju.domain.category.dto.FindCategoryResponse;
import com.study.jeonggiju.domain.category.dto.PublicCategoryResponse;
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

	@Transactional(readOnly = true)
	public FindCategoryResponse find(UUID categoryId){
		Category category = categoryRepository.findById(categoryId).orElseThrow();
		return FindCategoryResponse
			.builder()
			.id(category.getId())
			.title(category.getTitle())
			.description(category.getDescription())
			.recordType(category.getRecordType())
			.visibility(category.getVisibility())
			.build();

	}

	@Transactional(readOnly = true)
	public List<FindCategoryResponse> findAll(UUID userId){
		List<Category> categories = categoryRepository.findAllByUserId(userId);
		List<FindCategoryResponse> result = new ArrayList();
		for(Category category : categories) {
			result.add(FindCategoryResponse
				.builder()
				.id(category.getId())
				.title(category.getTitle())
				.description(category.getDescription())
				.recordType(category.getRecordType())
				.visibility(category.getVisibility())
				.build());
		}
		return result;

	}

	@Transactional
	public void save(UUID userId, AddCategory dto){
		User user = userRepository.findById(userId).orElseThrow();
		Category category = Category.of(dto.getTitle(), dto.getDescription(), dto.getRecordType(), user, dto.getVisibility());
		categoryRepository.save(category);
	}

	@Transactional
	public void update(UpdateCategory dto){
		Category category = categoryRepository.findById(dto.getId()).orElseThrow();
		category.update(dto.getTitle(), dto.getDescription(), dto.getVisibility());
		categoryRepository.save(category);
	}

	@Transactional
	public void delete(UUID id){
		categoryRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public List<PublicCategoryResponse> findPublic(){

		List<Category> categories = categoryRepository.findPublicCategoriesWithOwner();

		Map<UUID, PublicCategoryResponse> map = new LinkedHashMap<>();

		for(Category category: categories) {
			User user = category.getUser();

			PublicCategoryResponse response = map.computeIfAbsent(
				user.getId(),
				uuid -> PublicCategoryResponse.builder()
					.email(user.getEmail())
					.categories(new ArrayList<>())
					.build()
			);

			response.getCategories().add(
				PublicCategoryResponse.PublicCategoryElement.builder()
					.categoryId(category.getId())
					.title(category.getTitle())
					.recordType(category.getRecordType())
					.build()
			);
		}
		return new ArrayList<>(map.values());
	}
}
