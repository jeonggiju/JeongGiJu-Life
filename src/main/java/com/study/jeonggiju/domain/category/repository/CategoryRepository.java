package com.study.jeonggiju.domain.category.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.jeonggiju.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

	Optional<List<Category>> findAllByUserId(UUID userId);
}
