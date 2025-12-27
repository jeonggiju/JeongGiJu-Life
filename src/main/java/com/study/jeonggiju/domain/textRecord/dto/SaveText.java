package com.study.jeonggiju.domain.textRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class SaveText {
	private UUID categoryId;
	private String text;
	private String title;
	private LocalDate date;
}
