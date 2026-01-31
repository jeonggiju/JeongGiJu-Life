package com.life.jeonggiju.domain.expenseRecord.exception;

import java.time.LocalDate;
import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class ExpenseRecordNotFoundException extends ExpenseRecordException {
	public ExpenseRecordNotFoundException() {
		super(ErrorCode.EXPENSE_RECORD_NOT_FOUND);
	}

	public static ExpenseRecordNotFoundException withId(UUID id) {
		ExpenseRecordNotFoundException exception = new ExpenseRecordNotFoundException();
		exception.addDetail("expenseRecordId", id);
		return exception;
	}

	public static ExpenseRecordNotFoundException withCategoryIdAndDate(UUID categoryId, LocalDate date) {
		ExpenseRecordNotFoundException exception = new ExpenseRecordNotFoundException();
		exception.addDetail("categoryId", categoryId);
		exception.addDetail("date", date);
		return exception;
	}
}
