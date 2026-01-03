package com.life.jeonggiju.domain.user.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateReplyRequest {

	private UUID categoryId;
	private UUID parentId;
	private String comment;
}
