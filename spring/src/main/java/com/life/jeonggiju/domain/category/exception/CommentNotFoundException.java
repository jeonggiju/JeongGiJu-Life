package com.life.jeonggiju.domain.category.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class CommentNotFoundException extends CommentException {
	public CommentNotFoundException() {
		super(ErrorCode.COMMENT_NOT_FOUND);
	}

	public static CommentNotFoundException withId(UUID id) {
		CommentNotFoundException exception = new CommentNotFoundException();
		exception.addDetail("commentId", id);
		return exception;
	}
}
