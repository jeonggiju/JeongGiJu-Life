package com.life.jeonggiju.domain.category.dto;

import com.life.jeonggiju.domain.category.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryWithLikeCountDto {
	Category category;
	long likeCount;
}
