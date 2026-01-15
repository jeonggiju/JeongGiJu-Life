package com.life.jeonggiju.domain.numberRecord.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindNumberAllResponse {

	private int year;
	private int month;
	private int day;
	private List<Content> contents;

	@Data
	@Builder
	public static class Content {
		private UUID id;
		private double number;
		private LocalDate date;

	}
}
