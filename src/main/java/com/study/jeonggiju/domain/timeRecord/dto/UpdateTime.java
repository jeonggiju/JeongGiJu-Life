package com.study.jeonggiju.domain.timeRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class UpdateTime {
	private UUID id;
	private LocalDate date;
}
