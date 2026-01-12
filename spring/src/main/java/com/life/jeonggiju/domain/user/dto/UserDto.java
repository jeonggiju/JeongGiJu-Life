package com.life.jeonggiju.domain.user.dto;

import java.util.UUID;

import com.life.jeonggiju.domain.user.entity.Authority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
	UUID id;
	String email;
	String name;
	String title;
	String description;
	Authority authority;
	int birthYear;
	int birthMonth;
	int birthDay;
}
