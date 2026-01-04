package com.life.jeonggiju.security.jwt.registry;

import com.life.jeonggiju.security.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class JwtRegistryInformation {
    private UUID userId;
    private String accessToken;
    private String refreshToken;

    public void rotate(String newAccess, String newRefresh){
        this.accessToken = newAccess;
        this.refreshToken = newRefresh;
    }
}
