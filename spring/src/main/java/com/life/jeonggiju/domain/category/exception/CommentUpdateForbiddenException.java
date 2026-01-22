package com.life.jeonggiju.domain.category.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class CommentUpdateForbiddenException extends CommentException {
	public CommentUpdateForbiddenException() {
		super(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
	}

	public static CommentUpdateForbiddenException withIds(UUID commentId, UUID userId) {
		CommentUpdateForbiddenException exception = new CommentUpdateForbiddenException();
		exception.addDetail("commentId", commentId);
		exception.addDetail("userId", userId);
		return exception;
	}
}
