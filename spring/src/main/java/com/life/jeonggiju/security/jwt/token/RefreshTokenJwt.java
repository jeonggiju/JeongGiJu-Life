package com.life.jeonggiju.security.jwt.token;

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
    private final RefreshTokenUserInfo user;

    @Getter
    @Builder
    public static class RefreshTokenUserInfo {
        private UUID userId;
        private String username;
        private String userEmail;
    }
}
