package com.life.jeonggiju.domain.user.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCommentResponse {
	UUID commentId;
}
