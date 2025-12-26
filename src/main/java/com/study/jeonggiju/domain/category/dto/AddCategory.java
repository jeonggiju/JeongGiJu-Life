package com.study.jeonggiju.domain.category.dto;

import com.study.jeonggiju.domain.category.entity.RecordType;

import lombok.Data;

@Data
public class AddCategory {
	private String description;
	private RecordType recordType;
	private String title;
}
