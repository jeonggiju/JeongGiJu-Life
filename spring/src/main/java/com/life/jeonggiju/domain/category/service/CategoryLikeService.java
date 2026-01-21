package com.life.jeonggiju.domain.category.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.dto.CheckCountCategoryResponse;
import com.life.jeonggiju.domain.category.dto.LikePersonResponse;
import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.entity.CategoryLike;
import com.life.jeonggiju.domain.category.exception.CategoryNotFoundException;
import com.life.jeonggiju.domain.category.exception.DuplicateLikeException;
import com.life.jeonggiju.domain.category.repository.CategoryLikeRepository;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.notification.dto.NotificationCreatedDto;
import com.life.jeonggiju.domain.notification.entity.NotificationType;
import com.life.jeonggiju.domain.notification.service.NotificationService;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryLikeService {

	private final NotificationService notificationService;

	private final CategoryLikeRepository categoryLikeRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;


	@Transactional(readOnly = true)
	public List<LikePersonResponse> findUserEmails(UUID categoryId) {
		Category category = categoryRepository.findById(categoryId).orElseThrow(()->CategoryNotFoundException.withId(categoryId));
		List<CategoryLike> likes = category.getCategoryLikes();
		List<LikePersonResponse> response = new ArrayList<>();

		for (CategoryLike like : likes) {
			response.add(new LikePersonResponse(like.getUser().getEmail()));
		}

		return response;
	}

	@Transactional(readOnly = true)
	public int countByCategoryId(UUID categoryId) {
		return categoryLikeRepository.countByCategoryId(categoryId);
	}

	@Transactional
	public void add(UUID userId, UUID categoryId) {
		if (categoryLikeRepository.existsByUserIdAndCategoryId(userId, categoryId)) {
			throw new DuplicateLikeException();
		}

		User user = userRepository.findById(userId).orElseThrow(()->UserNotFoundException.withUserId(userId));
		Category category = categoryRepository.findById(categoryId).orElseThrow(()->CategoryNotFoundException.withId(categoryId));

		categoryLikeRepository.save(CategoryLike.of(user, category));

		UUID receiverId = category.getUser().getId();
		UUID senderId = user.getId();
		NotificationCreatedDto dto = NotificationCreatedDto.builder()
			.receiverId(receiverId)
			.senderId(senderId)
			.data(Map.of("categoryTitle", category.getTitle(), "senderEmail", user.getEmail()))
			.type(NotificationType.LIKE)
			.build();
		notificationService.notify(dto);
	}

	@Transactional(readOnly = true)
	public CheckCountCategoryResponse checkCount(UUID userId, UUID categoryId) {
		Optional<CategoryLike> categoryLikeOpt = categoryLikeRepository.findByUserIdAndCategoryId(userId, categoryId);
		boolean isCheck = categoryLikeOpt.isPresent();
		Integer count = categoryLikeRepository.countByCategoryId(categoryId);

		return CheckCountCategoryResponse.builder()
			.isChecked(isCheck)
			.count(count)
			.categoryId(categoryId)
			.build();
	}

	@Transactional
	public void delete(UUID userId, UUID categoryId) {
		categoryLikeRepository.deleteByUserIdAndCategoryId(userId, categoryId);
	}

}
