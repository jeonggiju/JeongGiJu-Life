package com.life.jeonggiju.domain.category.dto;

import java.time.Instant;
import java.util.UUID;

import com.life.jeonggiju.domain.category.entity.RecordType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDto {
	private UUID categoryId;
	private String categoryTitle;
	private String categoryDesc;
	private UUID authorId;
	private String authorNickname;
	private String authorTitle;
	private String authorDesc;
	private String authorEmail;
	private String authorProfileImageUrl;
	private RecordType type;
	private boolean hasLike;
	private long likeCount;
	private long commentCount;
	private Instant createdAt;
}