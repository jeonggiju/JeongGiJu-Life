package com.life.jeonggiju.domain.textRecord.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindTextAllResponse {

	private int birthYear;
	private int birthMonth;
	private int birthDay;
	private List<Content> contents;

	@Data
	@Builder
	public static class Content {
		private UUID id;
		private String title;
		private String text;
		private LocalDate date;
		private List<String> imageUrls;
	}
}
