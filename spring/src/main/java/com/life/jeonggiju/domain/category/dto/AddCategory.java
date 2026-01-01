package com.life.jeonggiju.domain.category.dto;

import com.life.jeonggiju.domain.category.entity.RecordType;
import com.life.jeonggiju.domain.category.entity.Visibility;

import lombok.Data;

@Data
public class AddCategory {
	private String description;
	private RecordType recordType;
	private String title;
	private Visibility visibility;
}
