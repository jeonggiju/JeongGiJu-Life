package com.life.jeonggiju.domain.checkListRecord.exception;

import java.time.LocalDate;
import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class CheckListRecordNotFoundException extends CheckListRecordException {
	public CheckListRecordNotFoundException() {
		super(ErrorCode.CHECK_LIST_RECORD_NOT_FOUND);
	}

	public static CheckListRecordNotFoundException withId(UUID id) {
		CheckListRecordNotFoundException exception = new CheckListRecordNotFoundException();
		exception.addDetail("checkListRecordId", id);
		return exception;
	}

	public static CheckListRecordNotFoundException withCategoryIdAndDate(UUID categoryId, LocalDate date) {
		CheckListRecordNotFoundException exception = new CheckListRecordNotFoundException();
		exception.addDetail("categoryId", categoryId);
		exception.addDetail("date", date);
		return exception;
	}
}
