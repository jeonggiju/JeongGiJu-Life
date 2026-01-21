package com.life.jeonggiju.domain.category.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class CategoryException extends BaseException {
	public CategoryException(ErrorCode code) {
		super(code);
	}
}
