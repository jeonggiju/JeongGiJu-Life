package com.life.jeonggiju.security.dto;

import com.life.jeonggiju.security.principal.LifeUserDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtLoginSuccessDto {
    private String accessToken;
    private LifeUserDto user;
}
