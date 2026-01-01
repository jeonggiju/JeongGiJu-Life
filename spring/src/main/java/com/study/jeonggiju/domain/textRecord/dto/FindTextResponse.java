package com.study.jeonggiju.domain.textRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindTextResponse {

	private UUID id;
	private String title;
	private String text;
	private LocalDate date;
}
