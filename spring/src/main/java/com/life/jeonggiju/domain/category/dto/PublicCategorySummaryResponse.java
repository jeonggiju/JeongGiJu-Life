package com.life.jeonggiju.domain.category.dto;

import java.util.List;
import java.util.UUID;

import com.life.jeonggiju.domain.category.entity.RecordType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PublicCategorySummaryResponse {
	String nextCursor;
	UUID nextIdAfter;
	boolean hasNext;
	long totalCount;
	String sortBy;
	String sortDirection;
	List<Content> contents;

	@Builder
	@Data
	@AllArgsConstructor
	public static class Content {
		UUID categoryId;
		String categoryTitle;
		String categoryDesc;
		RecordType type;
		boolean hasLike;
		boolean isMyCategory;
		long commentCount;
		long likeCount;

		String authorNickname;
		String authorEmail;
		String authorTitle;
		String authorDesc;
	}
}
