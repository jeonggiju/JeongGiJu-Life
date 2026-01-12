package com.life.jeonggiju.security.authentication.jwt.dto;

import com.life.jeonggiju.security.core.dto.UserPrincipal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtDto {
	UserPrincipal principal;
	String accessToken;
}

