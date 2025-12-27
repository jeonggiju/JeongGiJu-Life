package com.study.jeonggiju.domain.checkRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class UpdateCheck {
	private UUID id;
	private LocalDate date;
	private boolean success;
}
