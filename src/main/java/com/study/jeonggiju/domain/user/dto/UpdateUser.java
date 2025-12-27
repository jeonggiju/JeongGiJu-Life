package com.study.jeonggiju.domain.user.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class UpdateUser {
	private String title;
	private String description;
}
