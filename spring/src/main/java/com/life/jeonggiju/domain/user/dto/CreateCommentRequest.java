package com.life.jeonggiju.domain.user.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateCommentRequest {

	private UUID categoryId;
	private String comment;
}
