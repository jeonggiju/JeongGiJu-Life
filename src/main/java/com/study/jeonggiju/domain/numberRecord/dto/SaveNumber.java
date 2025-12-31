package com.study.jeonggiju.domain.numberRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class SaveNumber {
	private UUID categoryId;
	private double number;
	private LocalDate date;
}
