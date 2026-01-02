package com.life.jeonggiju.domain.category.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckCountCategoryResponse {
	private UUID categoryId;
	private boolean isChecked;
	private int count;
}
