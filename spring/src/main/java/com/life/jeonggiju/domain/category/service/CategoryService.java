package com.life.jeonggiju.domain.category.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.dto.AddCategory;
import com.life.jeonggiju.domain.category.dto.CategorySummaryDto;
import com.life.jeonggiju.domain.category.dto.FindCategoryResponse;
import com.life.jeonggiju.domain.category.dto.LikeEmailCategoryResponse;
import com.life.jeonggiju.domain.category.dto.PublicCategorySortKey;
import com.life.jeonggiju.domain.category.dto.PublicCategorySummaryResponse;
import com.life.jeonggiju.domain.category.dto.SortDir;
import com.life.jeonggiju.domain.category.dto.UpdateCategory;
import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.entity.CategoryLike;
import com.life.jeonggiju.domain.category.exception.CategoryNotFoundException;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.category.repository.CommentRepository;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public FindCategoryResponse find(UUID categoryId) {
		Category category = categoryRepository.findById(categoryId).orElseThrow(() -> CategoryNotFoundException.withId(categoryId));

		long likeCount = category.getCategoryLikes().size();
		long commentCount = category.getComments().size();

		return FindCategoryResponse
			.builder()
			.id(category.getId())
			.title(category.getTitle())
			.description(category.getDescription())
			.recordType(category.getRecordType())
			.visibility(category.getVisibility())
			.likeCount(likeCount)
			.commentCount(commentCount)
			.build();
	}

	@Transactional(readOnly = true)
	public List<FindCategoryResponse> findAll(UUID userId) {
		List<Category> categories = categoryRepository.findAllByUserId(userId);
		List<FindCategoryResponse> result = new ArrayList();
		long likeCount = 0;
		long commentCount = 0;
		for (Category category : categories) {
			likeCount = category.getCategoryLikes().size();
			commentCount = category.getComments().size();
			result.add(FindCategoryResponse
				.builder()
				.id(category.getId())
				.title(category.getTitle())
				.description(category.getDescription())
				.recordType(category.getRecordType())
				.visibility(category.getVisibility())
				.likeCount(likeCount)
				.commentCount(commentCount)
				.build());
		}
		return result;

	}

	@Transactional(readOnly = true)
	public List<LikeEmailCategoryResponse> findEmailLikeUser(UUID categoryId) {
		Category category = categoryRepository.findById(categoryId).orElseThrow(() -> CategoryNotFoundException.withId(categoryId));

		List<LikeEmailCategoryResponse> result = new ArrayList<>();
		List<CategoryLike> categoryLike = category.getCategoryLikes();

		for (CategoryLike like : categoryLike) {
			result.add(
				LikeEmailCategoryResponse.builder()
					.email(like.getUser().getEmail())
					.build());
		}
		return result;
	}

	@Transactional
	public void save(UUID userId, AddCategory dto) {
		User user = userRepository.findById(userId).orElseThrow(()-> UserNotFoundException.withUserId(userId));
		Category category = Category.of(dto.getTitle(), dto.getDescription(), dto.getRecordType(), user,
			dto.getVisibility());
		categoryRepository.save(category);
	}

	@Transactional
	public void update(UpdateCategory dto) {
		Category category = categoryRepository.findById(dto.getId()).orElseThrow(() -> CategoryNotFoundException.withId(dto.getId()));
		category.update(dto.getTitle(), dto.getDescription(), dto.getVisibility());
		categoryRepository.save(category);
	}

	@Transactional
	public void delete(UUID id) {
		categoryRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public PublicCategorySummaryResponse findPublicCategoriesSummary(
		UUID userId,
		List<PublicCategorySortKey> sortKeys,
		SortDir sortDir,
		String cursor,
		UUID idAfter,
		int size,
		String search
	) {
		List<CategorySummaryDto> categories = categoryRepository.findPublicCategoriesWithPagination(
			userId,
			sortKeys,
			sortDir,
			cursor,
			idAfter,
			size + 1,
			search
		);
		boolean hasNext = categories.size() > size;
		if (hasNext) {
			categories = categories.subList(0, size);
		}
		String nextCursor = null;
		UUID nextIdAfter = null;
		if (hasNext && !categories.isEmpty()) {
			CategorySummaryDto lastCategory = categories.get(categories.size() - 1);
			nextCursor = buildCursor(lastCategory, sortKeys);
			nextIdAfter = lastCategory.getCategoryId();
		}

		long totalCount = categoryRepository.countPublicCategories(search);

		List<PublicCategorySummaryResponse.Content> contents = categories.stream()
			.map(category -> PublicCategorySummaryResponse.Content.builder()
				.categoryId(category.getCategoryId())
				.categoryTitle(category.getCategoryTitle())
				.categoryDesc(category.getCategoryDesc())
				.authorNickname(category.getAuthorNickname())
				.authorEmail(category.getAuthorEmail())
				.authorTitle(category.getAuthorTitle())
				.authorDesc(category.getAuthorDesc())
				.type(category.getType())
				.hasLike(category.isHasLike())
				.isMyCategory(userId != null && userId.equals(category.getAuthorId()))
				.likeCount(category.getLikeCount())
				.commentCount(category.getCommentCount())
				.build())
			.collect(Collectors.toList());

		String sortBy = sortKeys.stream()
			.map(Enum::name)
			.collect(Collectors.joining(","));

		return PublicCategorySummaryResponse.builder()
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.hasNext(hasNext)
			.totalCount(totalCount)
			.sortBy(sortBy)
			.sortDirection(sortDir.name())
			.contents(contents)
			.build();
	}

	private String buildCursor(CategorySummaryDto category, List<PublicCategorySortKey> sortKeys) {
		if (sortKeys == null || sortKeys.isEmpty()) {
			sortKeys = List.of(PublicCategorySortKey.createdAt);
		}

		List<String> values = new ArrayList<>();

		for (PublicCategorySortKey key : sortKeys) {
			switch (key) {
				case likeCount:
					values.add(String.valueOf(category.getLikeCount()));
					break;
				case commentCount:
					values.add(String.valueOf(category.getCommentCount()));
					break;
				case title:
					values.add(category.getCategoryTitle() != null ? category.getCategoryTitle() : "");
					break;
				case createdAt:
					values.add(category.getCreatedAt() != null ? category.getCreatedAt().toString() : "");
					break;
				case authorNickname:
					values.add(category.getAuthorNickname() != null ? category.getAuthorNickname() : "");
					break;
			}
		}

		return String.join("|", values);
	}
}
