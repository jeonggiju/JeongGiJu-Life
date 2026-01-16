package com.life.jeonggiju.domain.category.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.life.jeonggiju.domain.category.dto.CategorySummaryDto;
import com.life.jeonggiju.domain.category.dto.PublicCategorySortKey;
import com.life.jeonggiju.domain.category.dto.SortDir;
import com.life.jeonggiju.domain.category.entity.QCategory;
import com.life.jeonggiju.domain.category.entity.QCategoryLike;
import com.life.jeonggiju.domain.category.entity.QComment;
import com.life.jeonggiju.domain.category.entity.RecordType;
import com.life.jeonggiju.domain.category.entity.Visibility;
import com.life.jeonggiju.domain.user.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryQueryDsl {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<CategorySummaryDto> findPublicCategoriesWithPagination(
		UUID userId,
		List<PublicCategorySortKey> sortKeys,
		SortDir sortDir,
		String cursor,
		UUID idAfter,
		int size,
		String search
	) {
		QCategory category = QCategory.category;
		QUser user = QUser.user;
		QCategoryLike categoryLike = QCategoryLike.categoryLike;
		QComment qComment = QComment.comment1;

		NumberExpression<Long> likeCountExpr = Expressions.asNumber(
			JPAExpressions
				.select(categoryLike.count())
				.from(categoryLike)
				.where(categoryLike.category.id.eq(category.id))
		).coalesce(0L);

		NumberExpression<Long> commentCountExpr = Expressions.asNumber(
			JPAExpressions
				.select(qComment.count())
				.from(qComment)
				.where(qComment.category.id.eq(category.id))
		).coalesce(0L);

		BooleanExpression userLikeExpression;
		if (userId != null) {
			userLikeExpression = JPAExpressions
				.selectOne()
				.from(categoryLike)
				.where(
					categoryLike.category.id.eq(category.id),
					categoryLike.user.id.eq(userId)
				)
				.exists();
		} else {
			userLikeExpression = Expressions.FALSE;
		}

		JPAQuery<Tuple> query = queryFactory
			.select(
				category.id,
				category.title,
				category.description,
				user.id,
				user.username,
				category.recordType,
				userLikeExpression,
				likeCountExpr,
				commentCountExpr,
				category.createdAt
			)
			.from(category)
			.join(category.user, user)
			.where(category.visibility.eq(Visibility.PUBLIC));

		if (search != null && !search.trim().isEmpty()) {
			String searchTerm = search.trim();
			BooleanExpression searchCondition = category.title.containsIgnoreCase(searchTerm)
				.or(category.description.containsIgnoreCase(searchTerm))
				.or(user.username.containsIgnoreCase(searchTerm));
			query.where(searchCondition);
		}

		if (cursor != null && idAfter != null) {
			BooleanExpression cursorCondition = buildCursorCondition(
				sortKeys,
				sortDir,
				cursor,
				idAfter,
				category,
				user,
				likeCountExpr,
				commentCountExpr
			);
			if (cursorCondition != null) {
				query.where(cursorCondition);
			}
		}

		query = applyOrdering(query, sortKeys, sortDir, category, user, likeCountExpr, commentCountExpr);
		query.limit(size);
		List<Tuple> results = query.fetch();
		return results.stream()
			.map(tuple -> CategorySummaryDto.builder()
				.categoryId(tuple.get(0, UUID.class))
				.categoryTitle(tuple.get(1, String.class))
				.categoryDesc(tuple.get(2, String.class))
				.authorId(tuple.get(3, UUID.class))
				.authorNickname(tuple.get(4, String.class))
				.type(tuple.get(5, RecordType.class))
				.hasLike(Boolean.TRUE.equals(tuple.get(6, Boolean.class)))
				.likeCount(tuple.get(7, Long.class) != null ? tuple.get(7, Long.class) : 0L)
				.commentCount(tuple.get(8, Long.class) != null ? tuple.get(8, Long.class) : 0L)
				.createdAt(tuple.get(9, Instant.class))
				.build())
			.collect(Collectors.toList());
	}

	@Override
	public long countPublicCategories(String search) {
		QCategory category = QCategory.category;
		QUser user = QUser.user;

		JPAQuery<Long> query = queryFactory
			.select(category.count())
			.from(category)
			.join(category.user, user)
			.where(category.visibility.eq(Visibility.PUBLIC));

		if (search != null && !search.trim().isEmpty()) {
			String searchTerm = search.trim();
			BooleanExpression searchCondition = category.title.containsIgnoreCase(searchTerm)
				.or(category.description.containsIgnoreCase(searchTerm))
				.or(user.username.containsIgnoreCase(searchTerm));
			query.where(searchCondition);
		}

		Long count = query.fetchOne();

		return count != null ? count : 0L;
	}

	private BooleanExpression buildCursorCondition(
		List<PublicCategorySortKey> sortKeys,
		SortDir sortDir,
		String cursor,
		UUID idAfter,
		QCategory category,
		QUser user,
		NumberExpression<Long> likeCountExpr,
		NumberExpression<Long> commentCountExpr
	) {
		String[] cursorValues = cursor.split("\\|");
		if (cursorValues.length != sortKeys.size()) {
			return null;
		}

		BooleanExpression condition = null;

		for (int i = 0; i < sortKeys.size(); i++) {
			BooleanExpression keyCondition = buildSingleKeyCondition(
				sortKeys,
				sortDir,
				cursorValues,
				idAfter,
				i,
				category,
				user,
				likeCountExpr,
				commentCountExpr
			);

			if (keyCondition != null) {
				condition = (condition == null) ? keyCondition : condition.or(keyCondition);
			}
		}

		return condition;
	}

	private BooleanExpression buildSingleKeyCondition(
		List<PublicCategorySortKey> sortKeys,
		SortDir sortDir,
		String[] cursorValues,
		UUID idAfter,
		int currentIndex,
		QCategory category,
		QUser user,
		NumberExpression<Long> likeCountExpr,
		NumberExpression<Long> commentCountExpr
	) {
		BooleanExpression condition = null;

		for (int i = 0; i < currentIndex; i++) {
			BooleanExpression equalCondition = buildEqualCondition(
				sortKeys.get(i),
				cursorValues[i],
				category,
				user,
				likeCountExpr,
				commentCountExpr
			);

			if (equalCondition != null) {
				condition = (condition == null) ? equalCondition : condition.and(equalCondition);
			}
		}

		BooleanExpression compareCondition = buildCompareCondition(
			sortKeys.get(currentIndex),
			sortDir,
			cursorValues[currentIndex],
			category,
			user,
			likeCountExpr,
			commentCountExpr
		);

		if (compareCondition != null) {
			condition = (condition == null) ? compareCondition : condition.and(compareCondition);
		}

		if (currentIndex == sortKeys.size() - 1 && idAfter != null) {
			BooleanExpression idCondition = sortDir == SortDir.asc
				? category.id.gt(idAfter)
				: category.id.lt(idAfter);
			condition = (condition == null) ? idCondition : condition.and(idCondition);
		}

		return condition;
	}

	private BooleanExpression buildEqualCondition(
		PublicCategorySortKey key,
		String value,
		QCategory category,
		QUser user,
		NumberExpression<Long> likeCountExpr,
		NumberExpression<Long> commentCountExpr
	) {
		switch (key) {
			case likeCount:
				return likeCountExpr.eq(Long.parseLong(value));
			case commentCount:
				return commentCountExpr.eq(Long.parseLong(value));
			case title:
				return category.title.eq(value);
			case createdAt:
				return category.createdAt.eq(Instant.parse(value));
			case authorNickname:
				return user.username.eq(value);
			default:
				return null;
		}
	}

	private BooleanExpression buildCompareCondition(
		PublicCategorySortKey key,
		SortDir sortDir,
		String value,
		QCategory category,
		QUser user,
		NumberExpression<Long> likeCountExpr,
		NumberExpression<Long> commentCountExpr
	) {
		boolean isAsc = sortDir == SortDir.asc;

		switch (key) {
			case likeCount:
				long likeCount = Long.parseLong(value);
				return isAsc ? likeCountExpr.gt(likeCount) : likeCountExpr.lt(likeCount);
			case commentCount:
				long commentCount = Long.parseLong(value);
				return isAsc ? commentCountExpr.gt(commentCount) : commentCountExpr.lt(commentCount);
			case title:
				return isAsc ? category.title.gt(value) : category.title.lt(value);
			case createdAt:
				Instant instant = Instant.parse(value);
				return isAsc ? category.createdAt.gt(instant) : category.createdAt.lt(instant);
			case authorNickname:
				return isAsc ? user.username.gt(value) : user.username.lt(value);
			default:
				return null;
		}
	}

	private JPAQuery<Tuple> applyOrdering(
		JPAQuery<Tuple> query,
		List<PublicCategorySortKey> sortKeys,
		SortDir sortDir,
		QCategory category,
		QUser user,
		NumberExpression<Long> likeCountExpr,
		NumberExpression<Long> commentCountExpr
	) {
		for (PublicCategorySortKey key : sortKeys) {
			OrderSpecifier<?> orderSpecifier = createOrderSpecifier(
				key,
				sortDir,
				category,
				user,
				likeCountExpr,
				commentCountExpr
			);
			if (orderSpecifier != null) {
				query.orderBy(orderSpecifier);
			}
		}

		if (sortDir == SortDir.asc) {
			query.orderBy(category.id.asc());
		} else {
			query.orderBy(category.id.desc());
		}

		return query;
	}

	private OrderSpecifier<?> createOrderSpecifier(
		PublicCategorySortKey key,
		SortDir sortDir,
		QCategory category,
		QUser user,
		NumberExpression<Long> likeCountExpr,
		NumberExpression<Long> commentCountExpr
	) {
		Order order = sortDir == SortDir.asc ? Order.ASC : Order.DESC;

		switch (key) {
			case likeCount:
				return new OrderSpecifier<>(order, likeCountExpr);
			case commentCount:
				return new OrderSpecifier<>(order, commentCountExpr);
			case title:
				return new OrderSpecifier<>(order, category.title);
			case createdAt:
				return new OrderSpecifier<>(order, category.createdAt);
			case authorNickname:
				return new OrderSpecifier<>(order, user.username);
			default:
				return null;
		}
	}
}