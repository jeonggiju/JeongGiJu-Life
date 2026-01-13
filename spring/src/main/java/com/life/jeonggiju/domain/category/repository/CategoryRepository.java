package com.life.jeonggiju.domain.category.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.life.jeonggiju.domain.category.dto.PublicCategorySummaryResponse;
import com.life.jeonggiju.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

	List<Category> findAllByUserId(UUID userId);

	@Query("""
		    select new com.life.jeonggiju.domain.category.dto.PublicCategorySummaryResponse(
		        c.id,
		        c.title,
		        c.description,
		        u.nickname,
		        c.recordType,
		        false,
		        false,
		        count(cl)
		    )
		    from Category c
		    join c.user u
		    left join c.categoryLikes cl
		    where c.visibility = com.life.jeonggiju.domain.category.entity.Visibility.PUBLIC
		    group by c.id, c.title, c.description, u.nickname, c.recordType
		    order by u.id, c.id
		""")
	List<PublicCategorySummaryResponse> findPublicSummariesAnonymous();

	@Query("""
		    select new com.life.jeonggiju.domain.category.dto.PublicCategorySummaryResponse(
		        c.id,
		        c.title,
		        c.description,
		        u.nickname,
		        c.recordType,
		        case when count(clMine) > 0 then true else false end,
		        case when u.id = :userId then true else false end,
		        count(clAll)
		    )
		    from Category c
		    join c.user u
		    left join c.categoryLikes clAll
		    left join CategoryLike clMine
		        on clMine.category = c and clMine.user.id = :userId
		    where c.visibility = com.life.jeonggiju.domain.category.entity.Visibility.PUBLIC
		    group by c.id, c.title, c.description, u.nickname, c.recordType, u.id
		    order by u.id, c.id
		""")
	List<PublicCategorySummaryResponse> findPublicSummaries(@Param("userId") UUID userId);

	boolean existsByIdAndUserId(UUID categoryId, UUID userId);
}
