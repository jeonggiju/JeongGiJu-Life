package com.life.jeonggiju.domain.expenseRecord.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindExpenseTagResponse {
	private UUID id;
	private String name;
}
