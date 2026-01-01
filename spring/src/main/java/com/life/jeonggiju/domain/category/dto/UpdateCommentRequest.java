package com.life.jeonggiju.domain.category.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateCommentRequest {

	private UUID commentId;
	private String comment;
}
