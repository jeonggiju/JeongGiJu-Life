package com.life.jeonggiju.security.jwt;

import com.life.jeonggiju.security.dto.AccessTokenUserDto;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AccessTokenJwt {
    private final String token;
    private final Instant issueTime;
    private final Instant expirationTime;
    private final AccessTokenUserDto user;
}
