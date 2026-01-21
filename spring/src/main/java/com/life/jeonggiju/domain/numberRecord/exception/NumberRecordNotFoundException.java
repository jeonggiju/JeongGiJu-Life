package com.life.jeonggiju.domain.numberRecord.exception;

import java.time.LocalDate;
import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class NumberRecordNotFoundException extends NumberRecordException {
	public NumberRecordNotFoundException() {
		super(ErrorCode.NUMBER_RECORD_NOT_FOUND);
	}

	public static NumberRecordNotFoundException withId(UUID id) {
		NumberRecordNotFoundException exception = new NumberRecordNotFoundException();
		exception.addDetail("numberRecordId", id);
		return exception;
	}

	public static NumberRecordNotFoundException withCategoryIdAndDate(UUID categoryId, LocalDate date) {
		NumberRecordNotFoundException exception = new NumberRecordNotFoundException();
		exception.addDetail("categoryId", categoryId);
		exception.addDetail("date", date);
		return exception;
	}
}
