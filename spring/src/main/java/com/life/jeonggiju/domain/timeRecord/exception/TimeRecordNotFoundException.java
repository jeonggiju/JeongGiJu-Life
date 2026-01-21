package com.life.jeonggiju.domain.timeRecord.exception;

import java.time.LocalDate;
import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class TimeRecordNotFoundException extends TimeRecordException {
	public TimeRecordNotFoundException() {
		super(ErrorCode.TIME_RECORD_NOT_FOUND);
	}

	public static TimeRecordNotFoundException withId(UUID id) {
		TimeRecordNotFoundException exception = new TimeRecordNotFoundException();
		exception.addDetail("timeRecordId", id);
		return exception;
	}

	public static TimeRecordNotFoundException withCategoryIdAndDate(UUID categoryId, LocalDate date) {
		TimeRecordNotFoundException exception = new TimeRecordNotFoundException();
		exception.addDetail("categoryId", categoryId);
		exception.addDetail("date", date);
		return exception;
	}
}
