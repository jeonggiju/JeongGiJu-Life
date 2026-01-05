package com.life.jeonggiju.security.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtLoginSuccessDto {
    private String accessToken;
    private UserDto user;
}
