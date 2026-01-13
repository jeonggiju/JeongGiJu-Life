package com.life.jeonggiju.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpRequest {

	@NotNull
	private String username;

	@NotNull
	private String email;

	@NotNull
	private String password;
	private String title;
	private String description;

	@NotEmpty
	private int birthYear;

	@NotEmpty
	private int birthMonth;

	@NotEmpty
	private int birthDay;
}
