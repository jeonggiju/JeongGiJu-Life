package com.life.jeonggiju.domain.checkRecord.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindCheckAllResponse {

	private int birthYear;
	private int birthMonth;
	private int birthDay;
	private List<Content> contents;

	@Data
	@Builder
	public static class Content {
		private UUID id;
		private boolean success;
		private LocalDate date;
	}
}
