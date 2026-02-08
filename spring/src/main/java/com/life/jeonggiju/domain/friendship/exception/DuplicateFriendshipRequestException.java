package com.life.jeonggiju.domain.friendship.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class DuplicateFriendshipRequestException extends FriendshipException {
	public DuplicateFriendshipRequestException() {
		super(ErrorCode.DUPLICATE_FRIENDSHIP_REQUEST);
	}
}
