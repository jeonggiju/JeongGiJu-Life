package com.life.jeonggiju.domain.category.dto;

import java.util.List;
import java.util.UUID;

import com.life.jeonggiju.domain.category.entity.RecordType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicCategoryResponse{

	private String email;
	private List<PublicCategoryElement> categories;

	@Data
	@Builder
	public static class PublicCategoryElement{
		private UUID userId;
		private UUID categoryId;
		private String title;
		private RecordType recordType;
	}
}
