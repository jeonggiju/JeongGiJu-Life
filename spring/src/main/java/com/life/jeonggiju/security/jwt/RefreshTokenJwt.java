package com.life.jeonggiju.security.jwt;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RefreshTokenJwt {
    private final String token;
    private final Instant issueTime;
    private final Instant expirationTime;
    private final UUID userId;
}
