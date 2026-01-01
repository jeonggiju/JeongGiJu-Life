package com.life.jeonggiju.domain.category.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.category.entity.CategoryLike;
import com.life.jeonggiju.domain.category.repository.CategoryLikeRepository;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.repository.UserRepository;

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
