package com.life.jeonggiju.domain.checkRecord.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class SaveCheck {
	private UUID categoryId;
	private boolean success;
	private LocalDate date;
}
