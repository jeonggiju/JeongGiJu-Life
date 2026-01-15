package com.life.jeonggiju.domain.checkList.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindCheckListAllResponse {

	private int year;
	private int month;
	private int day;
	private List<Content> contents;

	@Data
	@Builder
	public static class Content {
		private UUID id;
		private String todo;
		private boolean success;
		private LocalDate date;
	}
}
