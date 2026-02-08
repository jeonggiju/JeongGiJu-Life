package com.life.jeonggiju.domain.friendship.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class FriendshipException extends BaseException {
	public FriendshipException(ErrorCode code) {
		super(code);
	}
}
