package com.life.jeonggiju.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
	private String username;
	private String email;
	private String title;
	private String description;
	private int birthYear;
	private int birthMonth;
	private int birthDay;
}
