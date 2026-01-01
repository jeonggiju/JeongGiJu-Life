package com.life.jeonggiju.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

	private boolean success;
	private String message;
	private String username;
}
