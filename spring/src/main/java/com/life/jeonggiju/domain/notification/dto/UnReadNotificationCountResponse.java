package com.life.jeonggiju.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnReadNotificationCountResponse {
	private int count;
}
