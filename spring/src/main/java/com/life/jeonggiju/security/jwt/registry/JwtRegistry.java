package com.life.jeonggiju.security.jwt.registry;

import java.util.UUID;

public interface JwtRegistry {

    void registerJwtInformation(JwtRegistryInformation information);

    void invalidateJwtInformationByUserId(UUID userId);

    boolean hasActiveJwtInformationByUserId(UUID userId);

    boolean hasActiveJwtInformationByAccessToken(String accessToken);

    boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

    void rotateJwtInformationByRefreshToken(String refreshToken, JwtRegistryInformation newInformation);

    void clearExpiredJwtInformation();
}
