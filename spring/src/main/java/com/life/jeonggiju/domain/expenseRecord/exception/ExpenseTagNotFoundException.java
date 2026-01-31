package com.life.jeonggiju.domain.expenseRecord.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class ExpenseTagNotFoundException extends ExpenseRecordException {
	public ExpenseTagNotFoundException() {
		super(ErrorCode.EXPENSE_TAG_NOT_FOUND);
	}

	public static ExpenseTagNotFoundException withId(UUID id) {
		ExpenseTagNotFoundException exception = new ExpenseTagNotFoundException();
		exception.addDetail("expenseTagId", id);
		return exception;
	}
}
