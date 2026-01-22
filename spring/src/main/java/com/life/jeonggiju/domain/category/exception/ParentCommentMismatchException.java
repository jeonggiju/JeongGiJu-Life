package com.life.jeonggiju.domain.category.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class ParentCommentMismatchException extends CommentException {
	public ParentCommentMismatchException() {
		super(ErrorCode.PARENT_COMMENT_MISMATCH);
	}

	public static ParentCommentMismatchException withIds(UUID categoryId, UUID parentId) {
		ParentCommentMismatchException exception = new ParentCommentMismatchException();
		exception.addDetail("categoryId", categoryId);
		exception.addDetail("parentId", parentId);
		return exception;
	}
}
