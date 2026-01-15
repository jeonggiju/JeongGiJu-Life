package com.life.jeonggiju.domain.category.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class CategoryNotFoundException extends BaseException {
	public CategoryNotFoundException() {
		super(ErrorCode.CATEGORY_NOT_FOUND);
	}
}
