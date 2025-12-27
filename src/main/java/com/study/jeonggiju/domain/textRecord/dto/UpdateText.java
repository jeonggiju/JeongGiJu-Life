package com.study.jeonggiju.domain.textRecord.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UpdateText {
	private UUID id;
	private String title;
	private String text;
}
