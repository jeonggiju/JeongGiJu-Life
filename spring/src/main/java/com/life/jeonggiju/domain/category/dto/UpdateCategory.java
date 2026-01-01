package com.life.jeonggiju.domain.category.dto;

import java.util.UUID;

import com.life.jeonggiju.domain.category.entity.Visibility;

import lombok.Data;

@Data
public class UpdateCategory {
	private UUID id;
	private String description;
	private String title;
	private Visibility visibility;
}
