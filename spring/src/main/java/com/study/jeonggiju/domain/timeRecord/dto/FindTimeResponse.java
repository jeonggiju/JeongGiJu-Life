package com.study.jeonggiju.domain.timeRecord.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindTimeResponse {
	private UUID id;
	private LocalTime time;
	private LocalDate date;
}