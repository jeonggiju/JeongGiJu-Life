package com.life.jeonggiju.domain.category.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class CategoryLikeException extends BaseException {
	public CategoryLikeException(ErrorCode code) {
		super(code);
	}
}
