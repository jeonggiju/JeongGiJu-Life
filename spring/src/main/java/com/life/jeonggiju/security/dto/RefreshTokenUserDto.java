package com.life.jeonggiju.security.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RefreshTokenUserDto {
    private UUID userId;
    private String username;
}