package com.life.jeonggiju.domain.friendship.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class FriendshipNotFoundException extends FriendshipException {

	public FriendshipNotFoundException() {
		super(ErrorCode.FRIENDSHIP_NOT_FOUND);
	}

	public static FriendshipNotFoundException withId(UUID friendshipId) {
		FriendshipNotFoundException exception = new FriendshipNotFoundException();
		exception.addDetail("friendshipId", friendshipId);
		return exception;
	}
}
