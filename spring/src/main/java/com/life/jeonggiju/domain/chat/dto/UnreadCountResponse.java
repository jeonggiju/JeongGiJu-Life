package com.life.jeonggiju.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponse {
	private int count;
}
