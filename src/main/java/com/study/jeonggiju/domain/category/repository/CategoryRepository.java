package com.study.jeonggiju.domain.category.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.study.jeonggiju.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {


	List<Category> findAllByUserId(UUID userId);

	@Query("""
    	select c
    		from Category c
    		join fetch c.user u
    		where c.visibility = com.study.jeonggiju.domain.category.entity.Visibility.PUBLIC
    		order by u.id, c.id
    """)
	List<Category> findPublicCategoriesWithOwner();
}
