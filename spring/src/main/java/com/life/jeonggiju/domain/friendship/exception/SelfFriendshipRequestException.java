package com.life.jeonggiju.domain.friendship.exception;

import com.life.jeonggiju.exception.ErrorCode;

public class SelfFriendshipRequestException extends FriendshipException {
	public SelfFriendshipRequestException() {
		super(ErrorCode.SELF_FRIENDSHIP_REQUEST);
	}
}
