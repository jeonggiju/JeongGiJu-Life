package com.life.jeonggiju.domain.expenseRecord.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class ExpenseRecordException extends BaseException {
	public ExpenseRecordException(ErrorCode code) {
		super(code);
	}
}
