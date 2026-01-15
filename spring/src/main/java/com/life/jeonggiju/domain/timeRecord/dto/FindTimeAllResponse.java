package com.life.jeonggiju.domain.timeRecord.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindTimeAllResponse {

	private int birthYear;
	private int birthMonth;
	private int birthDay;
	private List<Content> contents;

	@Data
	@Builder
	public static class Content {
		private UUID id;
		private LocalTime time;
		private LocalDate date;

	}
}