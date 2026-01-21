package com.life.jeonggiju.domain.numberRecord.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class NumberRecordException extends BaseException {
	public NumberRecordException(ErrorCode code) {
		super(code);
	}
}
