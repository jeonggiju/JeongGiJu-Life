package com.life.jeonggiju.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {
	private String title;
	private String description;
	private String nickName;
}
