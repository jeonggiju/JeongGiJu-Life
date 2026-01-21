package com.life.jeonggiju.domain.timeRecord.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class TimeRecordException extends BaseException {
	public TimeRecordException(ErrorCode code) {
		super(code);
	}
}
