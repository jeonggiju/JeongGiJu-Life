package com.life.jeonggiju.domain.checkList.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class UpdateCheckList {
	private UUID id;
	private LocalDate date;
	private String todo;
	private boolean success;
}
