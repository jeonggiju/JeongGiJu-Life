package com.life.jeonggiju.domain.notification.exception;

import java.util.UUID;

import com.life.jeonggiju.exception.ErrorCode;

public class NotificationNotFoundException extends NotificationException {
	public NotificationNotFoundException() {
		super(ErrorCode.NOTIFICATION_NOT_FOUND);
	}

	public static NotificationNotFoundException withId(UUID id) {
		NotificationNotFoundException exception = new NotificationNotFoundException();
		exception.addDetail("notificationId", id);
		return exception;
	}
}
