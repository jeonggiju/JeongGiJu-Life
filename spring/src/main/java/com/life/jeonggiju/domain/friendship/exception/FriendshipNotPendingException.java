package com.life.jeonggiju.domain.friendship.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class FriendshipNotPendingException extends FriendshipException {
	public FriendshipNotPendingException() {
		super(ErrorCode.FRIENDSHIP_REQUEST_NOT_PENDING);
	}
}
