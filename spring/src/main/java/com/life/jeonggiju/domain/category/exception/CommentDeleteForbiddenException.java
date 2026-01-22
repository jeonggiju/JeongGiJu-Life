package com.life.jeonggiju.domain.category.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class CommentDeleteForbiddenException extends CommentException {
	public CommentDeleteForbiddenException() {
		super(ErrorCode.COMMENT_DELETE_FORBIDDEN);
	}

	public static CommentDeleteForbiddenException withIds(UUID commentId, UUID userId) {
		CommentDeleteForbiddenException exception = new CommentDeleteForbiddenException();
		exception.addDetail("commentId", commentId);
		exception.addDetail("userId", userId);
		return exception;
	}
}
