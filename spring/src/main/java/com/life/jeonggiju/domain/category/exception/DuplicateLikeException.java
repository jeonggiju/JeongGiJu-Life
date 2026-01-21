package com.life.jeonggiju.domain.category.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class DuplicateLikeException extends  CategoryLikeException{
	public DuplicateLikeException() {
		super(ErrorCode.DUPLICATE_LIKE);
	}
}
