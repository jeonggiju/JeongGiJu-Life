package com.life.jeonggiju.domain.numberRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindNumberResponse {
	private UUID id;
	private double number;
	private LocalDate date;
}
