package com.life.jeonggiju.domain.expenseRecord.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UpdateExpenseTag {
	private UUID id;
	private String name;
}
