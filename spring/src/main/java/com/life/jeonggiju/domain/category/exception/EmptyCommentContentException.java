package com.life.jeonggiju.domain.category.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class EmptyCommentContentException extends CommentException {
	public EmptyCommentContentException() {
		super(ErrorCode.EMPTY_COMMENT_CONTENT);
	}
}
