package com.life.jeonggiju.domain.textRecord.exception;

import java.time.LocalDate;
import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class TextRecordNotFoundException extends TextRecordException {
	public TextRecordNotFoundException() {
		super(ErrorCode.TEXT_RECORD_NOT_FOUND);
	}

	public static TextRecordNotFoundException withId(UUID id) {
		TextRecordNotFoundException exception = new TextRecordNotFoundException();
		exception.addDetail("textRecordId", id);
		return exception;
	}

	public static TextRecordNotFoundException withCategoryIdAndDate(UUID categoryId, LocalDate date) {
		TextRecordNotFoundException exception = new TextRecordNotFoundException();
		exception.addDetail("categoryId", categoryId);
		exception.addDetail("date", date);
		return exception;
	}
}
