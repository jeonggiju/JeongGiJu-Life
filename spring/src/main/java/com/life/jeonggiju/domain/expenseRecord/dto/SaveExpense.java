package com.life.jeonggiju.domain.expenseRecord.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.life.jeonggiju.domain.expenseRecord.entity.ExpenseType;
import com.life.jeonggiju.domain.expenseRecord.entity.PaymentMethod;

import lombok.Data;

@Data
public class SaveExpense {
	private UUID categoryId;
	private double amount;
	private ExpenseType expenseType;
	private PaymentMethod paymentMethod;
	private String merchant;
	private String memo;
	private LocalDate date;
	private List<UUID> tagIds;
}
