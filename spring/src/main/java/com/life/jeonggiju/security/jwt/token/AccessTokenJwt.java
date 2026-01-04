package com.life.jeonggiju.security.jwt.token;

import com.life.jeonggiju.domain.user.entity.Authority;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class AccessTokenJwt {
    private final String token;
    private final Instant issueTime;
    private final Instant expirationTime;
    private final AccessTokenUserInfo user;

    @Getter
    @Builder
    public static class AccessTokenUserInfo {
        private UUID userId;
        private String username;
        private String userEmail;
        private Authority authority;
    }

}
