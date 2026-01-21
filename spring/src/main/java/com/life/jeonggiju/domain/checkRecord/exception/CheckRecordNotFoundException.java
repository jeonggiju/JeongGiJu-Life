package com.life.jeonggiju.domain.checkRecord.exception;

import java.time.LocalDate;
import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class CheckRecordNotFoundException extends CheckRecordException {
	public CheckRecordNotFoundException() {
		super(ErrorCode.CHECK_RECORD_NOT_FOUND);
	}

	public static CheckRecordNotFoundException withId(UUID id) {
		CheckRecordNotFoundException exception = new CheckRecordNotFoundException();
		exception.addDetail("checkRecordId", id);
		return exception;
	}

	public static CheckRecordNotFoundException withCategoryIdAndDate(UUID categoryId, LocalDate date) {
		CheckRecordNotFoundException exception = new CheckRecordNotFoundException();
		exception.addDetail("categoryId", categoryId);
		exception.addDetail("date", date);
		return exception;
	}
}
