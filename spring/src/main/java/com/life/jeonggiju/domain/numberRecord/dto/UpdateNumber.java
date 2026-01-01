package com.life.jeonggiju.domain.numberRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class UpdateNumber {
	private UUID id;
	private double number;
	private LocalDate date;
}
