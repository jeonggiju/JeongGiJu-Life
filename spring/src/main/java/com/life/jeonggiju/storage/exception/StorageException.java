package com.life.jeonggiju.storage.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class StorageException extends BaseException {
	public StorageException(ErrorCode code) {
		super(code);
	}

	public StorageException(ErrorCode code, Throwable cause) {
		super(code, cause);
	}
}
