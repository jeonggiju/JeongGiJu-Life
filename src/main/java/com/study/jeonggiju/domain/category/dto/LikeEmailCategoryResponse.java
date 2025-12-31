package com.study.jeonggiju.domain.category.dto;

import org.springframework.web.bind.annotation.GetMapping;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeEmailCategoryResponse {
	private String email;
}
