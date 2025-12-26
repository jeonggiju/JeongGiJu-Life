package com.study.jeonggiju.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpRequest {
	private String username;
	private String email;
	private String password;
	private String title;
	private String description;
}
