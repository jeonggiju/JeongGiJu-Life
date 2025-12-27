package com.study.jeonggiju.domain.timeRecord.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.Data;

@Data
public class UpdateTime {
	private UUID id;
	private LocalTime time;
	private LocalDate date;
}
