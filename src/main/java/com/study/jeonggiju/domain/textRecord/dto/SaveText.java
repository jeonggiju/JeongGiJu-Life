package com.study.jeonggiju.domain.textRecord.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class SaveText {
	private UUID categoryId;
	private String text;
	private String title;
}
