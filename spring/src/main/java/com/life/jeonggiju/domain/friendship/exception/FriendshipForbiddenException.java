package com.life.jeonggiju.domain.friendship.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class FriendshipForbiddenException extends FriendshipException {
	public FriendshipForbiddenException() {
		super(ErrorCode.FRIENDSHIP_FORBIDDEN);
	}
}
