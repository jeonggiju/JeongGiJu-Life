package com.study.jeonggiju.domain.checkList.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class SaveCheckList {
	private UUID categoryId;
	private String todo;
	private boolean success;
	private LocalDate date;
}
