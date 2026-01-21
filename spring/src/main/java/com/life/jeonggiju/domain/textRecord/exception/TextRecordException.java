package com.life.jeonggiju.domain.textRecord.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class TextRecordException extends BaseException {
	public TextRecordException(ErrorCode code) {
		super(code);
	}
}
