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
		int size
	) {
		QCategory category = QCategory.category;
		QUser user = QUser.user;
		QCategoryLike categoryLike = QCategoryLike.categoryLike;
		QComment qComment = QComment.comment1;

		// 서브쿼리를 NumberExpression으로 래핑 (coalesce 사용)
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

		// 서브쿼리: 현재 사용자의 좋아요 여부
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

		// 메인 쿼리
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

		// 커서 조건 추가
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

		// 정렬 추가
		query = applyOrdering(query, sortKeys, sortDir, category, user, likeCountExpr, commentCountExpr);

		// 페이지 크기 제한
		query.limit(size);

		// 쿼리 실행
		List<Tuple> results = query.fetch();

		// DTO 변환
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
	public long countPublicCategories() {
		QCategory category = QCategory.category;

		Long count = queryFactory
			.select(category.count())
			.from(category)
			.where(category.visibility.eq(Visibility.PUBLIC))
			.fetchOne();

		return count != null ? count : 0L;
	}

	/**
	 * 커서 조건 생성
	 */
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

	/**
	 * 단일 정렬 키에 대한 조건 생성
	 */
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
		BooleanExpression prefixEq = null;

		// 이전 키들은 모두 같아야 함
		for (int i = 0; i < currentIndex; i++) {
			BooleanExpression eq = buildEqualCondition(
				sortKeys.get(i),
				cursorValues[i],
				category,
				user,
				likeCountExpr,
				commentCountExpr
			);
			if (eq != null) {
				prefixEq = (prefixEq == null) ? eq : prefixEq.and(eq);
			}
		}

		PublicCategorySortKey key = sortKeys.get(currentIndex);
		String value = cursorValues[currentIndex];

		BooleanExpression cmp = buildCompareCondition(
			key, sortDir, value, category, user, likeCountExpr, commentCountExpr
		);

		if (cmp == null)
			return prefixEq; // 방어

		BooleanExpression result = cmp;

		// 마지막 키에서는 id tie-breaker를 "OR (equal AND idCond)"로 붙여야 함
		if (currentIndex == sortKeys.size() - 1 && idAfter != null) {
			BooleanExpression eqLast = buildEqualCondition(
				key, value, category, user, likeCountExpr, commentCountExpr
			);
			BooleanExpression idCond = (sortDir == SortDir.asc)
				? category.id.gt(idAfter)
				: category.id.lt(idAfter);

			result = cmp.or(eqLast.and(idCond));
		}

		return (prefixEq == null) ? result : prefixEq.and(result);
	}

	/**
	 * 동등 조건 생성 (key = value)
	 */
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

	/**
	 * 비교 조건 생성 (key < value 또는 key > value)
	 */
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

	/**
	 * 정렬 적용
	 */
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

		// 마지막에 ID로 정렬 (deterministic ordering)
		if (sortDir == SortDir.asc) {
			query.orderBy(category.id.asc());
		} else {
			query.orderBy(category.id.desc());
		}

		return query;
	}

	/**
	 * 개별 키에 대한 OrderSpecifier 생성
	 */
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