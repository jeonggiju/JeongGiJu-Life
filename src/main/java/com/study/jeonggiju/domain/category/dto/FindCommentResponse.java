package com.study.jeonggiju.domain.category.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindCommentResponse {

	private UUID commentId;

	private String authorEmail;

	private String comment;
	private LocalDateTime createdAt;

	private UUID parentId;
	private List<FindCommentResponse> children;

	private boolean mine;
	private boolean canDelete; // 작성자 OR 카테고리 주인
	private boolean canUpdate; // 작성자

	private boolean isUpdated;
}
