package com.study.jeonggiju.domain.categoryLike.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.study.jeonggiju.domain.categoryLike.entity.CategoryLike;

@Repository
public interface CategoryLikeRepository extends JpaRepository<CategoryLike, UUID> {

	Integer countByCategoryId(UUID categoryId);
	void deleteByUserIdAndCategoryId(UUID userId, UUID categoryId);
	boolean existsByUserIdAndCategoryId(UUID userId, UUID categoryId);
}
