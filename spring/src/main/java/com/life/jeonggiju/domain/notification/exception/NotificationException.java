package com.life.jeonggiju.domain.notification.exception;

import com.life.jeonggiju.exception.BaseException;
import com.life.jeonggiju.exception.ErrorCode;

public class NotificationException extends BaseException {
	public NotificationException(ErrorCode code) {
		super(code);
	}
}
