package com.life.jeonggiju.domain.timeRecord.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.Data;

@Data
public class SaveTime {
	private UUID categoryId;
	private LocalTime time;
	private LocalDate date;
}
