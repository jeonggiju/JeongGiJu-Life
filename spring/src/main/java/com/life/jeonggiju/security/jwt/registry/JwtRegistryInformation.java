package com.life.jeonggiju.security.jwt.registry;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JwtRegistryInformation {
    private UUID userId;
    private String accessToken;
    private String refreshToken;

    public void rotate(String newAccess, String newRefresh){
        this.accessToken = newAccess;
        this.refreshToken = newRefresh;
    }
}
