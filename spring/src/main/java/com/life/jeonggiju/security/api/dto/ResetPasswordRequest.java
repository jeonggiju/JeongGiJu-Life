package com.life.jeonggiju.security.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordRequest {
	private String username;
	private String email;
	private String newPassword;
}
