package com.study.jeonggiju.domain.category.dto;

import java.util.UUID;

import com.study.jeonggiju.domain.category.entity.RecordType;
import com.study.jeonggiju.domain.category.entity.Visibility;

import lombok.Data;

@Data
public class UpdateCategory {
	private UUID id;
	private String description;
	private String title;
	private Visibility visibility;
}
