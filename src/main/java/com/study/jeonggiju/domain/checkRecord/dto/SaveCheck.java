package com.study.jeonggiju.domain.checkRecord.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class SaveCheck {
	private UUID categoryId;
	private boolean success;
}
