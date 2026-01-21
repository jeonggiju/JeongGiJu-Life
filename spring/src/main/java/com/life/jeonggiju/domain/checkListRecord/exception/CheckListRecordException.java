package com.life.jeonggiju.domain.checkListRecord.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class CheckListRecordException extends BaseException {
	public CheckListRecordException(ErrorCode code) {
		super(code);
	}
}
