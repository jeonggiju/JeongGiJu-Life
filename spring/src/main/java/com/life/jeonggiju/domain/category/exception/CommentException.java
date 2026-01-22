package com.life.jeonggiju.domain.category.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class CommentException extends BaseException {
	public CommentException(ErrorCode code) {
		super(code);
	}
}
