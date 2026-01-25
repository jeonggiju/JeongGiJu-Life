package com.life.jeonggiju.domain.textRecord.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class TextRecordImageException extends TextRecordException {

	public TextRecordImageException(ErrorCode code) {
		super(code);
	}

	public static TextRecordImageException uploadFailed(UUID textRecordId, String reason) {
		TextRecordImageException exception = new TextRecordImageException(ErrorCode.TEXT_RECORD_IMAGE_UPLOAD_FAILED);
		exception.addDetail("textRecordId", textRecordId);
		exception.addDetail("reason", reason);
		return exception;
	}

	public static TextRecordImageException deleteFailed(String imageUrl, String reason) {
		TextRecordImageException exception = new TextRecordImageException(ErrorCode.TEXT_RECORD_IMAGE_DELETE_FAILED);
		exception.addDetail("imageUrl", imageUrl);
		exception.addDetail("reason", reason);
		return exception;
	}
}
