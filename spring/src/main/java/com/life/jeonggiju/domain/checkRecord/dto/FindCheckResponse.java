package com.life.jeonggiju.domain.checkRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindCheckResponse {
	private UUID id;
	private boolean success;
	private LocalDate date;
}
