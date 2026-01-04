package com.life.jeonggiju.domain.category.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.life.jeonggiju.domain.category.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

	boolean existsByIdAndUserId(UUID id, UUID userId);

	void deleteByIdAndUserId(UUID id, UUID userId);

	@Query("""
        select c
        from Comment c
        join fetch c.user u
        left join fetch c.parent p
        where c.category.id = :categoryId
        order by c.createdAt asc
    """)
	List<Comment> findByCategoryId(UUID categoryId);

}
