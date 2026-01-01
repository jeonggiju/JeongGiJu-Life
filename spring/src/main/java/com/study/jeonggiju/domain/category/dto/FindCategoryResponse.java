package com.study.jeonggiju.domain.category.dto;

import java.util.UUID;

import com.study.jeonggiju.domain.category.entity.RecordType;
import com.study.jeonggiju.domain.category.entity.Visibility;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindCategoryResponse {

	private UUID id;
	private String description;
	private String title;
	private RecordType recordType;
	private Visibility visibility;
}
