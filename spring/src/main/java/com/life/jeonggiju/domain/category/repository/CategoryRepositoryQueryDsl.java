package com.life.jeonggiju.domain.category.repository;

import java.util.List;
import java.util.UUID;

import com.life.jeonggiju.domain.category.dto.CategorySummaryDto;
import com.life.jeonggiju.domain.category.dto.PublicCategorySortKey;
import com.life.jeonggiju.domain.category.dto.SortDir;

public interface CategoryRepositoryQueryDsl {

	List<CategorySummaryDto> findPublicCategoriesWithPagination(
		UUID userId,
		List<PublicCategorySortKey> sortKeys,
		SortDir sortDir,
		String cursor,
		UUID idAfter,
		int size,
		String search
	);

	long countPublicCategories(String search);
}

