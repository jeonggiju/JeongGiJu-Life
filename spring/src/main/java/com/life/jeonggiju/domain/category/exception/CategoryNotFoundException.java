package com.life.jeonggiju.domain.category.exception;

import java.util.UUID;

import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class CategoryNotFoundException extends CategoryException {
	public CategoryNotFoundException() {
		super(ErrorCode.CATEGORY_NOT_FOUND);
	}

	public static CategoryNotFoundException withId(UUID id) {
		CategoryNotFoundException exception = new CategoryNotFoundException();
		exception.addDetail("categoryId", id);
		return exception;
	}
}
