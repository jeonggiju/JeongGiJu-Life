package com.life.jeonggiju.security.dto;

import com.life.jeonggiju.domain.user.entity.Authority;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AccessTokenUserDto {
    private UUID userId;
    private String username;
    private String userEmail;
    private Authority authority;
}
