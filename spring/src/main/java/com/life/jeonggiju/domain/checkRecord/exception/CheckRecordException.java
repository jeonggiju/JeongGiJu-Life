package com.life.jeonggiju.domain.checkRecord.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class CheckRecordException extends BaseException {
	public CheckRecordException(ErrorCode code) {
		super(code);
	}
}
