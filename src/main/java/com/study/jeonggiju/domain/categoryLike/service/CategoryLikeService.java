package com.study.jeonggiju.domain.categoryLike.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.category.repository.CategoryRepository;
import com.study.jeonggiju.domain.categoryLike.entity.CategoryLike;
import com.study.jeonggiju.domain.categoryLike.repository.CategoryLikeRepository;
import com.study.jeonggiju.domain.user.entity.User;
import com.study.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryLikeService {

	private final CategoryLikeRepository categoryLikeRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public int countByCategoryId(UUID categoryId){
		return categoryLikeRepository.countByCategoryId(categoryId);
	}

	@Transactional
	public void add(UUID userId, UUID categoryId){
		if (categoryLikeRepository.existsByUserIdAndCategoryId(userId, categoryId)) {
			throw new RuntimeException("중복 좋아요 불가.");
		}

		User user = userRepository.findById(userId).orElseThrow();
		Category category = categoryRepository.findById(categoryId).orElseThrow();

		categoryLikeRepository.save(CategoryLike.of(user, category));
	}

	@Transactional
	public void delete(UUID userId, UUID categoryId) {
		categoryLikeRepository.deleteByUserIdAndCategoryId(userId, categoryId);
	}
}
