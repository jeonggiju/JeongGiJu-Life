package com.life.jeonggiju.domain.category.dto;

import java.util.UUID;

import com.life.jeonggiju.domain.category.entity.RecordType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PublicCategorySummaryResponse {
	UUID categoryId;
	String categoryTitle;
	String categoryDesc;
	String authorNickname;
	RecordType type;
	boolean hasLike;
	boolean isMyCategory;
	long likeCount;
}
