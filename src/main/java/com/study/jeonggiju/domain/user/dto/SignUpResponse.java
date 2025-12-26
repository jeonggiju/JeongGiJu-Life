package com.study.jeonggiju.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpResponse {
	private boolean success;
	private String message;
}
