package com.life.jeonggiju.domain.category.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.life.jeonggiju.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID>, CategoryRepositoryQueryDsl {

	List<Category> findAllByUserId(UUID userId);

	boolean existsByIdAndUserId(UUID categoryId, UUID userId);
}
