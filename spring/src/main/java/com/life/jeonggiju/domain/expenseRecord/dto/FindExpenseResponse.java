package com.life.jeonggiju.domain.expenseRecord.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.life.jeonggiju.domain.expenseRecord.entity.ExpenseType;
import com.life.jeonggiju.domain.expenseRecord.entity.PaymentMethod;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindExpenseResponse {
	private UUID id;
	private double amount;
	private ExpenseType expenseType;
	private PaymentMethod paymentMethod;
	private String merchant;
	private String memo;
	private LocalDate date;
	private List<TagInfo> tags;

	@Data
	@Builder
	public static class TagInfo {
		private UUID id;
		private String name;
	}
}
